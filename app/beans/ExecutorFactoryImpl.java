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

import beans.config.Conf;
import models.ServerNode;

import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.context.ApplicationContext;
import server.ProcExecutor;
import server.WriteEventListener;
import beans.api.ExecutorFactory;
import beans.api.ProcessStreamHandler;

import javax.inject.Inject;

/**
 * A factory class for generating different process executors.
 * 
 * @author adaml
 *
 */
public class ExecutorFactoryImpl implements ExecutorFactory {
	
	private static Logger logger = LoggerFactory.getLogger( ExecutorFactoryImpl.class );

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private Conf conf;

    public WriteEventListener getExecutorWriteEventListener( String key ){
        WriteEventListener writeEventListener = (WriteEventListener) applicationContext.getBean("executorWriteEventListener");
        writeEventListener.setKey( key );
        writeEventListener.init();
        return writeEventListener;
    }

    public ProcessStreamHandler getProcessStreamHandler( String key ){
        ProcessStreamHandler streamHandler = (ProcessStreamHandler) applicationContext.getBean("processStreamHandler");
        streamHandler.setWriteEventListener( getExecutorWriteEventListener( key ) );
        return streamHandler;
    }

	@Override
	public ProcExecutor getBootstrapExecutor( ServerNode serverNode ) {

		logger.info("Creating bootstrap executor.");
        String key = getKey(serverNode);

        ProcessStreamHandler streamHandler = getProcessStreamHandler(key);
		ExecuteWatchdog watchdog = new ExecuteWatchdog(conf.cloudify.bootstrapCloudWatchDogProcessTimeoutMillis);

        ProcExecutor executor = (ProcExecutor) applicationContext.getBean( "bootstrapExecutor" );
		executor.setExitValue(0);
		executor.setWatchdog(watchdog);
		executor.setStreamHandler(streamHandler);
		executor.setId(key);

        return executor ;
	}

    @Override
    public DefaultExecuteResultHandler getResultHandler( String name){
        return new ExecuteResultHandlerImpl().setName(name);
    }

	@Override
	public ProcExecutor getDeployExecutor( ServerNode server ) {
	
		logger.info("Creating deploy executor.");
        String key = getKey(server);

        ProcessStreamHandler streamHandler = getProcessStreamHandler(key);
		ExecuteWatchdog watchdog = new ExecuteWatchdog(  conf.cloudify.bootstrapCloudWatchDogProcessTimeoutMillis );

        ProcExecutor executor = (ProcExecutor) applicationContext.getBean( "deployExecutor" );
		executor.setExitValue(1);
		executor.setWatchdog(watchdog);
		executor.setStreamHandler(streamHandler);
		executor.setId(key);

        return  executor;
	}



    private String getKey(ServerNode server) {
        return server.getId().toString();
    }


}
