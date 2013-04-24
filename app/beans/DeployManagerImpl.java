/*******************************************************************************
 * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package beans;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import beans.cloudify.CloudifyRestClient;
import models.ServerNode;

import models.Widget;
import models.WidgetInstance;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.ApplicationContext;
import server.DeployManager;
import server.ProcExecutor;
import server.exceptions.ServerException;
import beans.api.ExecutorFactory;
import beans.config.Conf;
import utils.Utils;

/**
 * This class deploys a recipe file vi cloudify non-interactive CLI. 
 * Each deploy forks a CLI process and stream the output.
 * 
 * @author Igor Goldenberg
 */
public class DeployManagerImpl implements DeployManager
{

    private static Logger logger = LoggerFactory.getLogger( DeployManagerImpl.class );

    @Inject
    private Conf conf;
    
    @Inject 
    private ExecutorFactory executorFactory;

    @Inject
    private CloudifyRestClient cloudifyRestClient;


    @Override
    public WidgetInstance uninstall( ServerNode serverNode ){
        WidgetInstance widgetInstance = serverNode.getWidgetInstance();
        String installName = widgetInstance.getInstallName();
        // TODO : maybe we should verify it is installed using the rest client?
        File script = widgetInstance.getRecipeType() == Recipe.Type.APPLICATION ? conf.cloudify.uninstallApplicationScript : conf.cloudify.uninstallServiceScript;
        CommandLine cmdLine = new CommandLine( script );
        cmdLine.addArgument( serverNode.getPublicIP() );
        cmdLine.addArgument( installName );
        execute(  cmdLine, serverNode );
        return widgetInstance;
    }


    /**
     *
     *
     * Decide if this is an application or a service
     *
     * if Application
     *          Use a rest API call to list all the
     *          http://IP:8100/service/applications
     *
     *if service
     *          Use a rest API to list all services on default
     *          http://IP:8100/service/applications/default/services
     *
     * view the results and decide if it is installed
     *
     * @param server
     * @param widget
     * @return
     */
    private boolean alreadyInstalled( ServerNode server, Widget widget, Recipe.Type recipeType ){

        try{
        switch ( recipeType ) {

            case APPLICATION:
            {
                return cloudifyRestClient.listApplications( server.getPublicIP() ).response.containsKey( widget.toInstallName() );
            }
            case SERVICE:
            {
                return cloudifyRestClient.listServices( server.getPublicIP(), "default" ).response.contains( widget.toInstallName() );
            }
        }
        }catch(Exception e){
            logger.error( "unable to decide if [{}] named [{}] is already installed on [{}]", new Object[]{recipeType, widget.toInstallName(), server} );
        }

        return false; // todo
    }


    // todo - replace widget with widgetInstance - since we have server 1to1 widgetInstance, we can simply transfer server here.
    // todo - Widget should only be a template. We are installing a single instance.
	public WidgetInstance fork(ServerNode server, Widget widget)
	{
        File unzippedDir = Utils.downloadAndUnzip( widget.getRecipeURL(), widget.getApiKey() );
        File recipeDir = unzippedDir;
        if ( widget.getRecipeRootPath() != null ) {
            recipeDir = new File( unzippedDir, widget.getRecipeRootPath() );
        }
        logger.info( "Deploying an instance for recipe at : [{}] ", recipeDir );

        Recipe.Type recipeType = new Recipe( recipeDir ).getRecipeType();

        if ( alreadyInstalled( server, widget, recipeType ) ){
            // TODO : use new events mechanism to alert installation is DONE!
            return widget.addWidgetInstance( server, recipeDir );
        }else{
            logger.info( "Deploying: [ServerIP={}] [recipe={}] [type={}]", new Object[]{server.getPublicIP(), recipeDir, recipeType.name()} );
            String recipePath = FilenameUtils.separatorsToSystem( recipeDir.getPath() );

            CommandLine cmdLine = new CommandLine( conf.cloudify.deployScript );
            cmdLine.addArgument( server.getPublicIP() );
            cmdLine.addArgument( recipePath );
            cmdLine.addArgument( widget.toInstallName() );
            cmdLine.addArgument( recipeType.commandParam );

            execute( cmdLine, server );
            return widget.addWidgetInstance( server, recipeDir );
        }
    }

    private void execute( CommandLine cmdLine, ServerNode server )
    {
        try {
            DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
            ProcExecutor executor = executorFactory.getDeployExecutor( server );
            executor.execute( cmdLine, ApplicationContext.get().conf().server.environment.getEnvironment(), resultHandler );
            logger.info( "The process instanceId: {}", executor.getId() );
        } catch ( ExecuteException e ) {
            logger.error( "Failed to execute process. Exit value: " + e.getExitValue(), e );

            throw new ServerException( "Failed to execute process. Exit value: " + e.getExitValue(), e );
        } catch ( IOException e ) {
            logger.error( "Failed to execute process", e );

            throw new ServerException( "Failed to execute process.", e );
        }
    }


    public void setConf( Conf conf )
    {
        this.conf = conf;
    }

    public void setCloudifyRestClient( CloudifyRestClient cloudifyRestClient )
    {
        this.cloudifyRestClient = cloudifyRestClient;
    }
}