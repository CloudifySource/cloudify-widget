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

import models.ServerNode;

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


	public ProcExecutor fork(ServerNode server, File recipe)
	{
		Recipe.Type recipeType = new Recipe( recipe ).getRecipeType();
		logger.info( "Deploying: [ServerIP={}] [recipe={}] [type={}]", new Object[]{server.getPublicIP(), recipe, recipeType.name()} );
		String recipePath = FilenameUtils.separatorsToSystem(recipe.getPath());
		
		CommandLine cmdLine = new CommandLine( conf.cloudify.deployScript );
		cmdLine.addArgument(server.getPublicIP());
		cmdLine.addArgument(recipePath);
		cmdLine.addArgument(recipeType.commandParam );
		
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

		ProcExecutor executor = executorFactory.getDeployExecutor( server );

		try
		{
			executor.execute(cmdLine, ApplicationContext.get().conf().server.environment.getEnvironment(), resultHandler);

			logger.info("The process instanceId: {}", executor.getId());
			return executor;
		} catch (ExecuteException e)
		{
			logger.error("Failed to execute process. Exit value: " + e.getExitValue(), e);

			throw new ServerException("Failed to execute process. Exit value: " + e.getExitValue(), e);
		} catch (IOException e)
		{
			logger.error("Failed to execute process", e);

			throw new ServerException("Failed to execute process.", e);
		}
	}


    public void setConf( Conf conf )
    {
        this.conf = conf;
    }
}