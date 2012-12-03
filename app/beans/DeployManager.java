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

import static server.Config.*;
import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import models.ServerNode;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import play.Logger;
import server.Config;
import server.ResMessages;
import server.ServerException;

/**
 * This class deploys a recipe file vi cloudify non-interactive CLI. 
 * Each deploy forks a CLI process and stream the output.
 * 
 * @author Igor Goldenberg
 */
public class DeployManager implements server.DeployManager
{
	// keep all widget instances key=instanceId, value=Executor
	private Hashtable<String, ProcExecutor> _intancesTable = new Hashtable<String, ProcExecutor>();

	final static class ProcessStreamHandler extends PumpStreamHandler
	 {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		@Override
		protected void createProcessOutputPump(InputStream is, OutputStream os)
		{
			super.createProcessOutputPump(is, baos);
		}
		
		public String getOutput()
		{
			return baos.toString();
		}
	 }
	 

	final static public class ProcExecutor extends DefaultExecutor
	{
		private String id;
		private String publicIP;
		private String privateIP;
		private File recipe;
		private String[] args;
		private long expirationTime;
		private ProcessStreamHandler procHandler;
		
		public ProcExecutor(ServerNode server, File recipe, String...args)
		{
			this.id = server.getId();
			
			this.publicIP = server.getPublicIP();
			this.privateIP = server.getPrivateIP();
			this.recipe = recipe;
			this.args = args;
			this.expirationTime = server.getExpirationTime();
			
			procHandler = new ProcessStreamHandler();
			setStreamHandler(procHandler);
		}

		public String getId()
		{
			return id;
		}
		
		public String getPublicServerIP()
		{
			return publicIP;
		}
		
		public String getPrivateServerIP()
		{
			return privateIP;
		}

		public File getRecipe()
		{
			return recipe;
		}

		public String[] getArgs()
		{
			return args;
		}

		public String getOutput()
		{
			return procHandler.getOutput();
		}
		
		public int getElapsedTimeMin()
		{
			long elapsedTime = expirationTime - System.currentTimeMillis();
			if ( elapsedTime <=0 )
				return 0;
			else
				return (int)TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
		}
	}

	static enum RecipeType
	{
		APPLICATION, SERVICE;
		
		static RecipeType getRecipeTypeByFileName( String fileName )
		{
			if ( fileName.endsWith(APPLICATION.getFileIdentifier()) )
				return APPLICATION;
			
			if ( fileName.endsWith(SERVICE.getFileIdentifier()) )
				return SERVICE;
			
			return null;
		}
		
		public String getCmdParam()
		{
			switch( this )
			{
				case APPLICATION: return "install-application";
				case SERVICE: return "install-service";
				default: return null;
			}
		}
		
		public String getFileIdentifier()
		{
			switch( this )
			{
				case APPLICATION: return  "application.groovy";
				case SERVICE: return "service.groovy";
				default: return null;
			}
		}
	}
	
	public ProcExecutor getExecutor(String id)
	{
		return _intancesTable.get(id);
	}
	
	public void destroyExecutor(String id)
	{
		_intancesTable.remove( id );
	}

	public ProcExecutor fork(ServerNode server, File recipe)
	{
		RecipeType recipeType = getRecipeType( recipe );
		Logger.info( String.format("Deploying: [ServerIP=%s] [recipe=%s] [type=%s]", server.getPublicIP(), recipe, recipeType.name()));

		CommandLine cmdLine = new CommandLine(CLOUDIFY_DEPLOY_SCRIPT);
		cmdLine.addArgument(server.getPublicIP());
		cmdLine.addArgument(recipe.getPath());
		cmdLine.addArgument(recipeType.getCmdParam());
		
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

		ExecuteWatchdog watchdog = new ExecuteWatchdog(Config.CLOUDIFY_DEPLOY_TIMEOUT);
		ProcExecutor executor = new ProcExecutor(server, recipe);
		
		executor.setExitValue(1);
		executor.setWatchdog(watchdog);

		try
		{
			executor.execute(cmdLine, resultHandler);

			Logger.info("The process instanceId: " + executor.getId());

			// keep the processID to pump an output stream
			_intancesTable.put(executor.getId(), executor);

			return executor;
		} catch (ExecuteException e)
		{
			Logger.error("Failed to execute process. Exit value: " + e.getExitValue(), e);

			throw new ServerException("Failed to execute process. Exit value: " + e.getExitValue(), e);
		} catch (IOException e)
		{
			Logger.error("Failed to execute process", e);

			throw new ServerException("Failed to execute process.", e);
		}
	}
	
	
   /** 
	* @return recipe type Application or Service by recipe directory.
	* @throws ServerException if found a not valid recipe file.
	**/
    protected RecipeType getRecipeType( File recipeDir )
	{
		String[] files = recipeDir.list( new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return RecipeType.getRecipeTypeByFileName( name ) != null;
			}
		} );
		   
		if ( files == null || files.length == 0 )
			throw new ServerException(ResMessages.getFormattedString("recipe_not_valid_1",
                    RecipeType.APPLICATION.getFileIdentifier(), RecipeType.SERVICE.getFileIdentifier()));
		
		if ( files.length > 1)
			throw new ServerException(ResMessages.getFormattedString("recipe_not_valid_2",
					RecipeType.APPLICATION.getFileIdentifier(), RecipeType.SERVICE.getFileIdentifier()));

		return RecipeType.getRecipeTypeByFileName(files[0]);
	}
}