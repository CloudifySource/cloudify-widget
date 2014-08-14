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
package beans;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import beans.config.Conf;
import beans.scripts.ScriptExecutor;
import cloudify.widget.common.CloudifyOutputUtils;
import cloudify.widget.common.asyncscriptexecutor.IAsyncExecution;
import controllers.WidgetAdmin;

import models.ServerNodeEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.i18n.Messages;

import models.ServerNode;
import models.Widget;
import models.Widget.Status;
import models.WidgetInstance;
import play.libs.Json;
import server.*;
import utils.CollectionUtils;
import utils.Utils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * This class provides ability to deploy/undeploy new widget by apiKey.
 * Before that the user must create an account by WidgetAdmin and register a new widget.
 * 
 * @author Igor Goldenberg
 * @see ServerPoolImpl
 * @see WidgetAdmin
 */
public class WidgetServerImpl implements WidgetServer
{
    private static Logger logger = LoggerFactory.getLogger( WidgetServerImpl.class );
    @Inject
    private ServerPool serverPool;

    @Inject
    private MailSender mailSender;

    @Inject
    private Conf conf;

    @Inject
    private DeployManager deployManager;
    
	@Inject
	private ScriptExecutor scriptExecutor;    

    // for logging purposes.
    private Set<Long> serverNodeIds = new HashSet<Long>();

    private static Map<Recipe.Type, Pattern> installationFinishedRegexMap = null;

    static {
        installationFinishedRegexMap = new HashMap<Recipe.Type, Pattern>();
        for ( Recipe.Type type  : Recipe.Type.values() ) {
            String pattern = type + " .* (installed|successfully) (installed|successfully)";
            installationFinishedRegexMap.put(type, Pattern.compile( pattern, Pattern.CASE_INSENSITIVE) );
        }
    }

    private List<String> filterOutputLines = new LinkedList<String>(  );
    private List<String> filterOutputStrings = new LinkedList<String>(  );

    @PostConstruct
    public void init(){
        Utils.addAllTrimmed( filterOutputLines,  StringUtils.split( conf.cloudify.removeOutputLines, "|" ));
        Utils.addAllTrimmed( filterOutputStrings,  StringUtils.split( conf.cloudify.removeOutputString, "|" ));
    }

    @Override
    public void uninstall( ServerNode server )
    {
        logger.info( "uninstalling [{}], [{}]", server, server.getWidgetInstance() );
        if ( server.isRemote() ){
            deployManager.uninstall( server );
        }else{
            undeploy( server );
        }

    }

    public WidgetInstance deploy( Widget widget, ServerNode server, String remoteAddress  )
	{
		widget.countLaunch();
		return deployManager.fork( server, widget );
	}
	
	public void undeploy( ServerNode serverNode )
	{
		serverPool.destroy( serverNode );
	}

    private static boolean isFinished( Recipe.Type recipeType, String line ){
    	if( logger.isDebugEnabled() ){
    		logger.debug("checking to see if [{}] has finished using [{}]", recipeType, line );
    	}
        Pattern pattern = installationFinishedRegexMap.get(recipeType);
        return pattern != null && !StringUtils.isEmpty(line) && pattern.matcher(line).matches();
    }

    @Override
    public Status getWidgetStatus(ServerNode server) {
        Status result = new Status();

        List<String> output = new LinkedList<String>();
        result.setOutput(output);


        // avoid autoboxing - causes NPEs.
        Long timeLeft = null;
        if ( server != null ){
            Long timeLeftFromServer = server.getTimeLeft();
            if ( timeLeftFromServer != null ){
                timeLeft = timeLeftFromServer;
            }

        }
        // server is remote we don't count time
        if (server != null && !server.isRemote() && timeLeft != null) {
            result.setTimeleft((int) TimeUnit.MILLISECONDS.toMinutes(timeLeft));
            result.setTimeleftMillis(timeLeft);

        }

        if (server == null || ( timeLeft != null && timeLeft.longValue() == 0 ) ) {
            logger.info("no more time left. trial time is over, settings status to STOPPED");
            result.setState(Status.State.STOPPED);
            output.add("i18n:testDriveSuccessfullyCompleted");
            return result;
        }else{
            result.setInstanceId( Long.toString(server.getId()) ); // will need this to register users on specific nodes.
        }


        IAsyncExecution bootstrapExecution = scriptExecutor.getBootstrapExecution(server);// need to sort out the cache before we decide if the installation finished.

        result.setRawOutput( bootstrapExecution.getOutputAsList() );

        result.setRemote( server.isRemote() ).setHasPemFile(!StringUtils.isEmpty(server.getPrivateKey())); // let UI know this is a remote bootstrap.

        boolean doneFromEvent = false;

        if ( !CollectionUtils.isEmpty(server.events) ){
            for (ServerNodeEvent event : server.events) {
                switch ( event.getEventType() ) {

                    case DONE:
                        logger.debug( "detected that widget instance installation done by event" );
                        doneFromEvent = true;
                        break;
                    case ERROR:
                    {
                        result.setState( Status.State.STOPPED );
                        result.setMessage( event.getMsg() );

                        logWidgetInstanceError( server, result, "WidgetInstance threw an error\n");

                        return result;
                    }
                    case INFO:
                    {
                        output.add( event.getMsg() );
                    }
                    break;
                    default:
                    {
                        logger.error( "unknown event type while formatting : [{}]", event.getEventType() );
                    }
                }
            }
        }

        output.addAll(CloudifyOutputUtils.formatOutput( bootstrapExecution.getOutput(), server.getPrivateIP() + "]", filterOutputLines, filterOutputStrings));

      	logger.debug( ">> output= [{}]" , Arrays.toString( output.toArray( new String[ output.size() ] ) ) );

        WidgetInstance widgetInstance = WidgetInstance.findByServerNode(server);
        if( logger.isDebugEnabled() ){
        	logger.debug("checking if installation finished for {} on the following output {}" , widgetInstance, output );
        }
        if (widgetInstance != null ){
            if (doneFromEvent || isFinished(widgetInstance.getRecipeType(), (String)CollectionUtils.last(output))){

                // need to figure out the remote service IP for the link
                if ( server.isRemote() && StringUtils.isEmpty( widgetInstance.getServicePublicIp() ) && !StringUtils.isEmpty( widgetInstance.getWidget().getConsoleUrlService() )  ){
                    // find out the service's public IP.
                    String servicePublicIp = deployManager.getServicePublicIp( widgetInstance );
                    if ( !StringUtils.isEmpty( servicePublicIp )){
                        logger.info( "found ip at : [{}]", servicePublicIp );

                    }else{
                        logger.info("could not find a public ip, defaulting to machine's public ip");
                        servicePublicIp = server.getPublicIP(); // default behavior.
                    }
                    widgetInstance.setServicePublicIp( servicePublicIp );
                    widgetInstance.save(  );
                }

                if( logger.isDebugEnabled() ){
                	logger.debug("detected finished installation");
                }
                output.add( "i18n:installationCompletedSuccessfully" );
                result.setCompleted(true);
                result.setInstanceIsAvailable(Boolean.TRUE);
                result.setConsoleLink(widgetInstance.getLink());
            }
        }

        result.setState(Status.State.RUNNING);
        if (!StringUtils.isEmpty(server.getPublicIP())) {
            result.setPublicIp(server.getPublicIP());
            result.setCloudifyUiIsAvailable(Boolean.TRUE);
        }



        try {
            if (server!= null && server.getBusySince() != null && ( System.currentTimeMillis() - server.getBusySince() > conf.cloudify.deployTimeoutError  ) ) {
                logWidgetInstanceError(server, result, "widgetInstance is taking too long. More than ", Long.toString(conf.cloudify.deployTimeoutError), " millis \n");
            }
        }catch(Exception e){
            logger.warn("unable to decide if widget is running for too long",e);
        }


        return result;
    }



    private void logWidgetInstanceError( ServerNode serverNode, Widget.Status status, String ... prefix ){

        if ( !serverNodeIds.contains( serverNode.getId() )){
            serverNodeIds.add( serverNode.getId() );
            StringBuilder sb = new StringBuilder();

            for (String s : prefix) {
                sb.append(s);
            }

            sb.append( Json.stringify(Json.toJson( status )) );

            logger.error(sb.toString());
        }

    }

    public void setServerPool(ServerPool serverPool) {
        this.serverPool = serverPool;
    }

    public void setDeployManager(DeployManager deployManager) {
        this.deployManager = deployManager;
    }

    public void setConf( Conf conf )
    {
        this.conf = conf;
    }
}