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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cloudify.widget.api.clouds.CloudServer;
import cloudify.widget.api.clouds.CloudServerApi;
import models.ServerNode;
import models.Widget;

import org.apache.commons.lang.NumberUtils;
import org.codehaus.jackson.JsonNode;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.Routes;
import play.cache.Cache;
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
import utils.StringUtils;
import utils.Utils;
import akka.util.Duration;
import beans.config.Conf;

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
public class Application extends Controller
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


            if ( !StringUtils.isEmptyOrSpaces( widget.getLoginVerificationUrl() ) ) {
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


//             ApplicationContext.get().getEventMonitor().eventFired( new Events.PlayWidget( request().remoteAddress(), widget ) );

            // credentials validation is made when we attempt to create a PEM file. if credentials are wrong, it will fail.
            RequestBody requestBody = request().body();
            JsonNode advancedData = null;
            JsonNode recipeProperties = null;

            if ( requestBody != null && requestBody.asJson() != null && !StringUtils.isEmptyOrSpaces( requestBody.asJson().toString() ) ){
                JsonNode jsonNode = requestBody.asJson();
                String ADVANCED_DATA_JSON_KEY = "advancedData";
                if ( jsonNode.has(ADVANCED_DATA_JSON_KEY) && !StringUtils.isEmptyOrSpaces( jsonNode.get(ADVANCED_DATA_JSON_KEY).toString())){
                    advancedData = jsonNode.get(ADVANCED_DATA_JSON_KEY);
                }

                String RECIPE_PROPERTIES_JSON_KEY = "recipeProperties";
                if ( jsonNode.has(RECIPE_PROPERTIES_JSON_KEY) && !StringUtils.isEmptyOrSpaces( jsonNode.get(RECIPE_PROPERTIES_JSON_KEY).toString())){

                    recipeProperties = jsonNode.get(RECIPE_PROPERTIES_JSON_KEY);
                }
            }

            if ( advancedData != null ){
                serverNode = new ServerNode();
                serverNode.setAdvancedParams( advancedData.toString() );
                serverNode.setRemote(true);
                serverNode.setWidget(widget);
                serverNode.save();
            }else{
                serverNode = ApplicationContext.get().getServerPool().get();
                logger.info("application will check if server node is null. if null, there are no available servers");
                if (serverNode == null) {
                    // if the user clicked another play in the last 30 seconds, we allow ourselves to tell them to wait..
                    Long timeLeft = getPlayTimeout();

                    if (timeLeft != null) {
                        throw new ServerException(Messages.get("please.wait.x.sec", (timeLeft - System.currentTimeMillis()) / 1000));
                    }
                    ApplicationContext.get().getMailSender().sendPoolIsEmptyMail( ApplicationContext.get().getServerPool().getStats().toString() );
                    throw new ServerException(Messages.get("no.available.servers"));
                }
                logger.info("it seems server node is not null. deployment continues as planned");
            }

            if ( recipeProperties != null ){
                serverNode.setRecipeProperties( recipeProperties.toString() );
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
                setPlayTimeout(remoteAddress);
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
                                }

                                logger.info("installing widget on cloud");
                                setPlayTimeout(remoteAddress);
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

    public static Result getInjectScript( String publicIp, String privateIp ){
        if ( Play.isDev() ){
            String injectedScript = ApplicationContext.get().getServerBootstrapper().getInjectedBootstrapScript( publicIp, privateIp );
            return ok(injectedScript);
        }else{
            return internalServerError("only available in dev mode");
        }
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

                        ApplicationContext.get().getServerBootstrapper().

                        if (managerIp == null) {
                            logger.info("did not find a manager to tear down");

                        }

                    }
                }
        );
            return ok("TBD");

    }

    private static Long getPlayTimeout(){
        Long timeLeft = (Long) Cache.get(Controller.request().remoteAddress());

        Conf conf = ApplicationContext.get().conf();
        if ( timeLeft != null ){
            Long delta =  timeLeft - System.currentTimeMillis();
            if ( delta < 0 || delta > conf.settings.stopTimeout ){
                logger.info("got a weird delta [{}], returning NULL", delta );
            }
            return null;
        }

        return timeLeft;
    }

    private static void setPlayTimeout( String remoteAddress ){
        try{
            Conf conf = ApplicationContext.get().conf();
            logger.info("counting the user [{}] millis before another deploy" , ApplicationContext.get().conf().settings.stopTimeout );
            // keep the user for 30 seconds by IP, to avoid immediate widget start after stop
            Cache.set( remoteAddress, System.currentTimeMillis() + conf.settings.stopTimeout, ( int ) (conf.settings.stopTimeout / 1000) );
        }catch(Exception e){
            logger.error("unable to set timeout for remoteAddress",e);
        }
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


    public static Result stopPoolInstance( final String apiKey, final String instanceId )
    {
        Akka.system().scheduler().scheduleOnce( Duration.create( 0, TimeUnit.SECONDS ),
                new Runnable() {
                    @Override
                    public void run()
                    {
                        logger.info( "uninstalling [{}], [{}]", apiKey, instanceId );

                        Widget widget = Widget.getWidget( apiKey );

//                        if ( widget != null ) {
//                            ApplicationContext.get().getEventMonitor().eventFired( new Events.StopWidget( remoteAddress , widget ) );
//                        }

                        if ( instanceId != null ) {
                            logger.info("stopping server node for widget [{}] and instanceId [{}]", widget, instanceId );
                            ServerNode serverNode = ServerNode.findByWidgetAndInstanceId(widget, instanceId);
                            if ( serverNode != null ) {
                                ApplicationContext.get().getServerBootstrapper().destroyServer( serverNode );
                            }else{
                                logger.info("serverNode for widget [{}] and instanceId [{}] does not exit", widget, instanceId );
                            }
//                            ApplicationContext.get().getWidgetServer().uninstall( serverNode );
//                            Utils.deleteCachedOutput( serverNode );
                        }
                    }
                } );
        return ok( OK_STATUS ).as( "application/json" );
    }

	
	public static Result getWidgetStatus( String apiKey, String instanceId )
	{
		try
		{
			if( logger.isDebugEnabled() ){
				logger.debug( "~~~ getWidgetStatus, instanceId=" + instanceId );
			}
            if (!NumberUtils.isNumber( instanceId )){
                return badRequest();
            }
            
            ServerNode serverNode = ServerNode.find.byId( Long.parseLong(instanceId) );
			Widget.Status wstatus = 
						ApplicationContext.get().getWidgetServer().getWidgetStatus(serverNode);
			if( logger.isDebugEnabled() ){
				logger.debug( "~~~ wstatus=" + wstatus );
			}
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

    public static Result javascriptRoutes()
    {
        response().setContentType( "text/javascript" );
        return ok(
                Routes.javascriptRouter( "jsRoutes",
                        // Routes for Projects
                        routes.javascript.WidgetAdmin.getAllWidgets(),
                        routes.javascript.WidgetAdmin.postWidget(),
                        routes.javascript.WidgetAdmin.checkPasswordStrength(),
                        routes.javascript.WidgetAdmin.postChangePassword(),
                        routes.javascript.WidgetAdmin.getPasswordMatch(),
                        routes.javascript.WidgetAdmin.postWidgetDescription(),
                        routes.javascript.WidgetAdmin.deleteWidget(),
                        routes.javascript.WidgetAdmin.postRequireLogin(),
                        routes.javascript.WidgetAdmin.regenerateWidgetApiKey(),
                        routes.javascript.WidgetAdmin.enableWidget(),
                        routes.javascript.WidgetAdmin.disableWidget(),

                        routes.javascript.Application.downloadPemFile(),
                        routes.javascript.Application.encrypt(),
                        routes.javascript.Application.decrypt(),

                        routes.javascript.AdminPoolController.addNode(),
                        routes.javascript.AdminPoolController.poolEvents(),
                        routes.javascript.AdminPoolController.removeNode(),
                        routes.javascript.AdminPoolController.checkAvailability(),
                        routes.javascript.AdminPoolController.summary(),
                        routes.javascript.AdminPoolController.getCloudServers(),
                        routes.javascript.AdminPoolController.getServerNodes(),
                        routes.javascript.AdminPoolController.getWidgetInstances(),
                        routes.javascript.AdminPoolController.getStatuses(),


                        routes.javascript.DemosController.listWidgetForDemoUser()

                )
        );

    }
}