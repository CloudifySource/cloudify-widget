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

import models.ServerNode;

import org.apache.commons.exec.ExecuteWatchdog;

import play.modules.spring.Spring;
import server.ApplicationContext;
import server.ProcExecutor;
import server.WriteEventListener;
import beans.ProcExecutorImpl.ProcessStreamHandler;
import beans.api.ExecutorFactory;

/**
 * A factory class for generating different process executors.
 * 
 * @author adaml
 *
 */
public class ExecutorFactoryImpl implements ExecutorFactory {
	
	@Override
	public ProcExecutor getBootstrapExecutor( String key ) {
		
		WriteEventListener writeEventListener = createWriteEventListener();
		writeEventListener.setKey(key);
		ProcessStreamHandler streamHandler = new ProcessStreamHandler(writeEventListener);
		
		ExecuteWatchdog watchdog = new ExecuteWatchdog(ApplicationContext.get().conf().cloudify.bootstrapCloudWatchDogProcessTimeoutMillis);
		ProcExecutor executor = (ProcExecutor) Spring.getBean("bootstrapExecutor");
		executor.setExitValue(0);
		executor.setWatchdog(watchdog);
		executor.setStreamHandler(streamHandler);
		return executor ;
	}

	@Override
	public ProcExecutor getDeployExecutor( ServerNode server, File recipe, String ... args ) {
		
		WriteEventListener writeEventListener = createWriteEventListener();
		writeEventListener.setKey(server.getId());
		ProcessStreamHandler streamHandler = new ProcessStreamHandler(writeEventListener);
		ExecuteWatchdog watchdog = new ExecuteWatchdog( ApplicationContext.get().conf().cloudify.bootstrapCloudWatchDogProcessTimeoutMillis );
		
		ProcExecutor executor = ( (ProcExecutor) Spring.getBean( "deployExecutor" ) );
		executor.setExitValue(1);
		executor.setWatchdog(watchdog);
		executor.setStreamHandler(streamHandler);
		executor.setRecipe( recipe );
		executor.setArgs( args );
		executor.setId(server.getId());
		return  executor;
	}

	private WriteEventListener createWriteEventListener() {
		WriteEventListener writeEventListener = ( WriteEventListener  ) Spring.getBean("executorFactoryWriteEventListener");
		return writeEventListener;
	}

}
