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
        logger.debug("checking to see if [{}] has finished using [{}]", recipeType, line );
        Pattern pattern = installationFinishedRegexMap.get(recipeType);
        return pattern != null && !StringUtils.isEmpty(line) && pattern.matcher(line).matches();
    }

    @Override
    public Status getWidgetStatus(ServerNode server) {
        Status result = new Status();



        List<String> output = new LinkedList<String>();
        result.setOutput(output);

        if (server == null) {
            result.setState(Status.State.STOPPED);
            output.add(Messages.get("test.drive.successfully.complete"));
            return result;
        }else{
            result.setInstanceId( Long.toString(server.getId()) ); // will need this to register users on specific nodes.
        }

        String cachedOutput = Utils.getCachedOutput( server );// need to sort out the cache before we decide if the installation finished.

        result.setRawOutput( Utils.split( cachedOutput, "\n" ) );

        result.setRemote( server.isRemote() ).setHasPemFile( !StringUtils.isEmpty(server.getPrivateKey()) ); // let UI know this is a remote bootstrap.

        boolean doneFromEvent = false;

        if ( !CollectionUtils.isEmpty(server.events) ){
            for (ServerNodeEvent event : server.events) {
                switch ( event.getEventType() ) {

                    case DONE:
                        logger.info( "detected that widget instance installation done by event" );
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

        output.addAll(Utils.formatOutput(cachedOutput, server.getPrivateIP() + "]", filterOutputLines, filterOutputStrings));

        WidgetInstance widgetInstance = WidgetInstance.findByServerNode(server);
        logger.debug("checking if installation finished for {} on the following output {}" , widgetInstance, output );
        if (widgetInstance != null ){
            if (doneFromEvent || isFinished(widgetInstance.getRecipeType(), (String)CollectionUtils.last(output))){

                // need to figure out the remote service IP for the link
                if ( server.isRemote() && StringUtils.isEmpty( widgetInstance.getServicePublicIp() ) && !StringUtils.isEmpty( widgetInstance.getWidget().getConsoleUrlService() )  ){
                    // find out the service's public IP.
                    String servicePublicIp = deployManager.getServicePublicIp( widgetInstance );
                    if ( !StringUtils.isEmpty( servicePublicIp )){
                        logger.info( "found ip at : [{}]", servicePublicIp );
                        widgetInstance.setServicePublicIp( servicePublicIp );
                        widgetInstance.save(  );
                    }
                }

                logger.debug("detected finished installation");
                output.add( "Installation completed successfully" );
                result.setInstanceIsAvailable(Boolean.TRUE);
                result.setConsoleLink(widgetInstance.getLink());
            }
        }

        result.setState(Status.State.RUNNING);
        if (!StringUtils.isEmpty(server.getPublicIP())) {
            result.setPublicIp(server.getPublicIP());
            result.setCloudifyUiIsAvailable(Boolean.TRUE);
        }

        // server is remote we don't count time
        Long timeLeft = server.getTimeLeft();
        if (!server.isRemote() && timeLeft != null) {
            result.setTimeleft((int) TimeUnit.MILLISECONDS.toMinutes(timeLeft));
            result.setTimeleftMillis(timeLeft);

        }

        logWidgetInstanceError( server, result, "widgetInstance is taking too long. More than ", Long.toString(conf.cloudify.deployTimeoutError) , " millis \n");

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