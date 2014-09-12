/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package controllers;

import static utils.RestUtils.OK_STATUS;

import java.io.File;
import java.io.FileFilter;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

import cloudify.widget.allclouds.executiondata.ExecutionDataModel;
import cloudify.widget.api.clouds.CloudProvider;
import cloudify.widget.api.clouds.CloudServer;
import cloudify.widget.api.clouds.CloudServerApi;
import cloudify.widget.common.StringUtils;
import models.*;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang.NumberUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.Routes;
import play.i18n.Messages;
import play.libs.Akka;
import play.libs.F;
import play.libs.Json;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import server.ApplicationContext;
import server.HeaderMessage;
import server.exceptions.ServerException;
import utils.CollectionUtils;
import akka.util.Duration;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.MySqlPlatform;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;

/**
 * Widget controller with the main functions like start(), stop(), getWidgetStatus().
 * 
 * @author Igor Goldenberg
 */
public class Application extends GsController
{

    private static Logger logger = LoggerFactory.getLogger(Application.class);

    /*@Inject
    private static Conf conf;

    @Value("${my.name}")
    private Conf privateConf;

    @PostConstruct
    public void init(){
        conf = privateConf;
    }
*/

    
    public static Result getConf( String name ){
    	
		JsonNode json = Json.toJson( ApplicationContext.get().conf().uiConf );
    	String jsVarResult ="var myConf=" + json.toString() + ";";
		return ok( jsVarResult );
    }
    
    // guy - todo - apiKey should be an encoded string that contains the userId and widgetId.
    //              we should be able to decode it, verify user's ownership on the widget and go from there.
    /**
     * Start does 2 things:
     *  - gets a server node
     *           - if remote bootstrap we need to bootstrap one from scratch
     *           - otherwise we take one from the pool.
     *
     *  - installs the widget recipe
     * @param apiKey - user api key
//     * @param project - the HP project API data
//     * @param key - the HP key API data
//     * @param secretKey - the HP secret key API data
//     * @param userId - in case required login authentication is used
     * @return - result
     */
	public static Result start( String apiKey/*, String project, String key, String secretKey*/, String userId )
	{
		try
		{
			
            //! logger.info( "starting widget with [apiKey, key, secretKey] = [{},{},{}]", new Object[]{apiKey, key, secretKey} );
			
 			Widget widget = Widget.getWidget( apiKey );
            ServerNode serverNode = null;
            if ( widget == null || !widget.isEnabled()) {
            	new HeaderMessage().setError( Messages.get("widget.disabled.by.administrator") ).apply( response().getHeaders() );
            	return badRequest(  );
            }


            if ( !StringUtils.isEmptyOrSpaces(widget.getLoginVerificationUrl()) ) {
                try {
                    F.Promise<WS.Response> post = WS.url( widget.getLoginVerificationUrl().replace( "$userId", userId ) ).post( "content" );
                    WS.Response response = post.get( 5L, TimeUnit.SECONDS );
                    if ( response.getStatus() != 200 ) {
                        return badRequest( "userId not verified : " + response.toString() );
                    }
                } catch ( Exception e ) {
                    logger.error( "error while validating userId [{}] on url [{}]", userId, widget.getLoginVerificationUrl() );
                }
            }



            // credentials validation is made when we attempt to create a PEM file. if credentials are wrong, it will fail.
            RequestBody requestBody = request().body();
            JsonNode executionData = null;
            JsonNode recipeProperties = null;

            if ( requestBody != null && requestBody.asJson() != null && !StringUtils.isEmptyOrSpaces( requestBody.asJson().toString() ) ){
                JsonNode jsonNode = requestBody.asJson();
                String EXECUTION_DATA_JSON = "executionData";
                if ( jsonNode.has(EXECUTION_DATA_JSON) && !StringUtils.isEmptyOrSpaces( jsonNode.get(EXECUTION_DATA_JSON).toString())){
                    executionData = jsonNode.get(EXECUTION_DATA_JSON);
                }
            }


            logger.info("deciding if remote by checking if recipe URL is null or not :: [{}]", widget.getRecipeURL()    );
            if ( !StringUtils.isEmptyOrSpaces(widget.getRecipeURL()) ){
                logger.info("server node is remote using solo mode");
                serverNode = new ServerNode();
                serverNode.setRemote(true);

                serverNode.save();
            }else{
                logger.info("not remote. using free trial");
                serverNode = ApplicationContext.get().getServerPool().get();
                logger.info("application will check if server node is null. if null, there are no available servers");
                if (serverNode == null) {
                    ApplicationContext.get().getMailSender().sendPoolIsEmptyMail( ApplicationContext.get().getServerPool().getStats().toString() );
                    throw new ServerException("i18n:noAvailableServers");
                }
                logger.info("it seems server node is not null. deployment continues as planned");
            }


            if ( executionData != null ) {
                logger.info("saving execution model on the server");
                ExecutionDataModel edm = ApplicationContext.get().getNewExecutionDataModel();
                edm.setEncryptionKey(ApplicationContext.get().conf().applicationSecret);
                edm.setRaw(executionData.toString());

                serverNode.setExecutionData(edm.encrypt());

                serverNode.setWidget(widget);
                serverNode.save();
            }

            // run the "bootstrap" and "deploy" in another thread.

            final ServerNode finalServerNode = serverNode;
            final Widget finalWidget = widget;
            final String remoteAddress = request().remoteAddress();
            logger.info("scheduling deployment");

            // TODO : this is a quick fix for the thread exhaustion problem. We need to figure out the best course of action here
            // TODO : we assume that recipes without URL are fast and so simply don't need a thread for it..
            // TODO : however the design should be ignorant to the recipeURL nullability and still scale.
            if ( StringUtils.isEmptyOrSpaces(widget.getRecipeURL()) ){
                logger.info("no recipe url. this should be quick. no need for thread");
                logger.info("installing widget on cloud");
                ApplicationContext.get().getWidgetServer().deploy(finalWidget, finalServerNode, remoteAddress);
            }else{
                logger.info("recipe url exists. will schedule Akka");
                Akka.system().scheduler().scheduleOnce(
                        Duration.create(0, TimeUnit.SECONDS),
                        new Runnable() {
                            @Override
                            public void run() {
                                logger.info("deployment thread started");
                                if (finalServerNode.isRemote()) {

                                    logger.info("trying to find existing management");
                                    try {
                                        String ip = getExistingManagement(finalWidget, finalServerNode.getExecutionDataModel());
                                        if ( !StringUtils.isEmptyOrSpaces(ip )) {
                                            logger.info("found management on ip [{}]", ip);
                                            finalServerNode.setPublicIP(ip);
                                            finalServerNode.save();
                                        }else{
                                            logger.info("did not find management");
                                        }
                                    }catch(Exception e){
                                        logger.info("got exception. will try to bootstrap cloud",e);
                                    }

                                    if ( StringUtils.isEmptyOrSpaces( finalServerNode.getPublicIP() )) {
                                        logger.info("bootstrapping remote cloud");
                                        try {
                                            if (ApplicationContext.get().getServerBootstrapper().bootstrapCloud(finalServerNode) == null) {
                                                logger.info("bootstrap cloud returned NULL. stopping progress.");
                                                return;
                                            }
                                        } catch (Exception e) {
                                            logger.error("unable to bootstrap machine", e);
                                            return;
                                        }
                                    }else{
                                        logger.info("skipping bootstrap");
                                    }
                                }

                                logger.info("installing widget on cloud");
                                ApplicationContext.get().getWidgetServer().deploy(finalWidget, finalServerNode, remoteAddress);
                            }
                        });
            }


            return statusToResult( new Widget.Status().setInstanceId(serverNode.getId().toString()).setRemote(serverNode.isRemote()) );
		}catch(ServerException ex)
		{
            return exceptionToStatus( ex );
		}
	}


    public static Result changePassword( ){
        try {

            User user = validateSession();


            JsonNode jsonNode = request().body().asJson();
            String currentPassword = jsonNode.get("currentPassword").getTextValue();

            if ( !user.comparePassword(currentPassword) ){
                return badRequest("wrong current password");
            }

            String newPassword = jsonNode.get("newPassword").getTextValue();
            String newPasswordAgain = jsonNode.get("newPasswordAgain").getTextValue();

            if ( !newPassword.equals(newPasswordAgain)){
                return badRequest("new password does not match");
            }

            user.encryptAndSetPassword( newPassword );
            user.save();
            return ok("password saved successfully");


        }catch(Exception e){
            return badRequest("please check currentPassword, newPassword and newPasswordAgain are sent on request");
        }
    }

    // download the file
    public static Result getInjectScript( String publicIp, String privateIp ){
        validateSession();
        String randomPassword = StringUtils.generateRandomFromRegex(ApplicationContext.get().conf().server.bootstrap.serverNodePasswordRegex);
        String injectedScript = ApplicationContext.get().getServerBootstrapper().getInjectedBootstrapScript( publicIp, privateIp, randomPassword);


        return ok(new ReaderInputStream( new StringReader(injectedScript)));
    }

    public static Result getPoolStatus(  ){
        String authToken = session("authToken");
        if ( User.validateAuthToken( authToken ) == null ) {
            return unauthorized();
        }
        return ok(Json.toJson(ApplicationContext.get().getServerPool().getStats()));
    }

    // find existing management and returns IP
    public static String getExistingManagement( Widget widget, ExecutionDataModel executionDataModel ){

        try {
            if (StringUtils.isEmptyOrSpaces(widget.managerPrefix)) {
                return null;
            }

            logger.info("searching for existing management using managerPrefix : " + widget.managerPrefix );
            CloudServerApi cloudServerApi = executionDataModel.advancedDataToCloudServerApi( widget.cloudProvider );

            Collection<CloudServer> allMachinesWithTag = cloudServerApi.getAllMachinesWithTag("");
            logger.info("found machines [{}]", CollectionUtils.size(allMachinesWithTag));

            for (CloudServer cloudServer : allMachinesWithTag) {
                logger.info("checking [{}] vs. [{}]", cloudServer.getName(), widget.managerPrefix );
                if (cloudServer.getName().startsWith(widget.managerPrefix)) {
                    return cloudServer.getServerIp().publicIp;
                }
            }
        }catch(Exception e){
            logger.error("unable to find existing management",e);
            return null;
        }

        return null;
    }


    public static Result tearDownRemoteBootstrap( final String widgetApiKey ){

        Akka.system().scheduler().scheduleOnce( Duration.create( 0, TimeUnit.SECONDS ),
                new Runnable() {
                    @Override
                    public void run() {
                        Widget widget = Widget.getWidget(widgetApiKey);

                        if (StringUtils.isEmptyOrSpaces(widget.managerPrefix)) {
                            logger.error("This widget is not configured for remote teardown. please contact admin.");
                            return;
                        }

                        logger.info("tearing down remote bootatrap");
                        CloudServerApi cloudServerApi = ApplicationContext.get().getCloudServerApi();

                        // credentials validation is made when we attempt to create a PEM file. if credentials are wrong, it will fail.
                        RequestBody requestBody = request().body();
                        JsonNode advancedData = null;
                        JsonNode recipeProperties = null;

                        if (requestBody != null && requestBody.asJson() != null && !StringUtils.isEmptyOrSpaces(requestBody.asJson().toString())) {
                            JsonNode jsonNode = requestBody.asJson();
                            String ADVANCED_DATA_JSON_KEY = "advancedData";
                            if (jsonNode.has(ADVANCED_DATA_JSON_KEY) && !StringUtils.isEmptyOrSpaces(jsonNode.get(ADVANCED_DATA_JSON_KEY).toString())) {
                                advancedData = jsonNode.get(ADVANCED_DATA_JSON_KEY);
                            }
                        }

                        String managerIp = null;
                        cloudServerApi.connect();
                        Collection<CloudServer> allMachinesWithTag = cloudServerApi.getAllMachinesWithTag(null);
                        for (CloudServer cloudServer : allMachinesWithTag) {
                            if (cloudServer.getName().startsWith(widget.managerPrefix)) {
                                managerIp = cloudServer.getServerIp().publicIp;
                            }
                        }

//                        ApplicationContext.get().getServerBootstrapper().

                        if (managerIp == null) {
                            logger.info("did not find a manager to tear down");

                        }

                    }
                }
        );
            return ok("TBD");

    }


    private static Result exceptionToStatus( Exception e ){
           Widget.Status status = new Widget.Status();
           status.setState(Widget.Status.State.STOPPED);
           status.setMessage(e.getMessage());
           return statusToResult(status);
       }

    public static Result downloadPemFile( String instanceId ){
        ServerNode serverNode = ServerNode.find.byId( Long.parseLong( instanceId ) );
        if ( serverNode != null && !StringUtils.isEmpty(serverNode.getPrivateKey()) ){
            response().setHeader("Content-Disposition", String.format("attachment; filename=privateKey_%s.pem", instanceId));
            return ok ( serverNode.getPrivateKey() );
        }
        return badRequest("instance stopped");
    }

    private static Result statusToResult( Widget.Status status ){
        Map<String,Object> result = new HashMap<String, Object>();
        result.put("status", status );
        logger.debug( "~~~ status=" + status );
        logger.debug("statusToResult > result: [{}]", result);
        return ok( Json.toJson( result ));
    }

    public static Result stopServerNode( Long serverNodeId ){
        validateSession();
        ServerNode serverNode = ServerNode.find.byId(serverNodeId);

        if ( serverNode != null ){
            serverNode.setStopped(true);
            serverNode.setBusySince(System.currentTimeMillis());
            serverNode.save();
        }

        return ok();
    }


    public static Result stopPoolInstance( final String apiKey, final String instanceId )
    {
        logger.info("uninstalling [{}], [{}]", apiKey, instanceId);
        Widget widget = Widget.getWidget(apiKey);
        if (instanceId != null) {
            logger.info("stopping server node for widget [{}] and instanceId [{}]", widget, instanceId);
            ServerNode serverNode = ServerNode.findByWidgetAndInstanceId(widget, instanceId);
            if (serverNode != null) {
                serverNode.setStopped(true);
                serverNode.save();
            } else {
                logger.info("serverNode for widget [{}] and instanceId [{}] does not exit", widget, instanceId);
            }
        }

        return ok(OK_STATUS).as("application/json");
    }

	
	public static Result getWidgetStatus( String apiKey, String instanceId )
	{
		try
		{
     		logger.debug( "getting status for instance [{}]", instanceId  );
            if (!NumberUtils.isNumber( instanceId )){
                return badRequest();
            }
            
            ServerNode serverNode = ServerNode.find.byId( Long.parseLong(instanceId) );

            Widget.Status wstatus =
						ApplicationContext.get().getWidgetServer().getWidgetStatus(serverNode);

			return statusToResult(wstatus);
		}catch(ServerException ex)
		{
			return exceptionToStatus( ex );
		}
	}

    public static Result generateDDL(){
        if ( Play.isDev() ) {
            EbeanServer defaultServer = Ebean.getServer( "default" );

            ServerConfig config = new ServerConfig();
            config.setDebugSql( true );

            DdlGenerator ddlGenerator = new DdlGenerator( ( SpiEbeanServer ) defaultServer, new MySqlPlatform(), config );
            String createDdl = ddlGenerator.generateCreateDdl();
            String dropDdl = ddlGenerator.generateDropDdl();
            return ok( createDdl );
        }else{
            return forbidden(  );
        }
    }

    public static Result encrypt(String data) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(ApplicationContext.get().conf().applicationSecret);
        return ok(textEncryptor.encrypt(data));
    }

    public static Result decrypt(String data) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(ApplicationContext.get().conf().applicationSecret);
        return ok(textEncryptor.decrypt(data));
    }


    public static Result getCloudProviders() {
        return ok (Json.toJson(CloudProvider.values()));
    }

    public static Result getCloudNames(  ){

        String cloudifyHome = ApplicationContext.get().conf().server.environment.cloudifyHome;
        File file = new File(cloudifyHome);

        logger.info("will find cloud providers for home dir [{}] ", file.getAbsolutePath());

        File cloudsFolder = new File(file,"clouds");

        File[] cloudProviders = cloudsFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        List<String> result = new LinkedList<String>();


        for (File cloudProvider : cloudProviders) {
            result.add(cloudProvider.getName());
        }
        Collections.sort(result);
        return ok(Json.toJson(result));

    }


    public static Result logout(){
        session().clear();
        response().discardCookies( "authToken" );
        return ok();
    }

    public static Result login( ){

        JsonNode jsonNode = request().body().asJson();
        String email = jsonNode.get("email").getTextValue();
        String password = jsonNode.get("password").getTextValue();
        User authenticated = User.authenticate(email, password);
        if ( authenticated != null ) {
            session("authToken", authenticated.getAuthToken());
            return ok();
        }else{
            return  badRequest();
        }


    }

    public static Result isLoggedIn(){
        Map<String, Boolean> result = new HashMap<String, Boolean>();
        result.put("loggedIn", Boolean.FALSE);
        try {
            String authToken = session("authToken");
            User user = User.validateAuthToken(authToken);

            if (user != null) {
                result.put("loggedIn", Boolean.TRUE);
            }
            return ok(Json.toJson(result));
        }catch(Exception e){

        }

        return ok(Json.toJson(result));
    }


    public static Result getUserDetails(){
        return ok(Json.toJson( validateSession() ));
    }


    public static Result cleanPool(){
        logger.info("got a request to clean pool");
        validateSession();
        ApplicationContext.get().getServerPool().clearPool();
        return ok();
    }

    public static Result getPoolNodesByStatus(){
        validateSession();
        return ok( Json.toJson(ApplicationContext.get().getServerPool().getPoolNodesByStatus() ) );
    }

}