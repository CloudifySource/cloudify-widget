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

package mocks;

import java.util.List;

import beans.BootstrapValidationResult;
import models.ServerNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.ServerBootstrapper;

public class ServerBootstrapperMock implements ServerBootstrapper 
{
    private static Logger logger = LoggerFactory.getLogger(ServerBootstrapperMock.class);

    @Override
    public void destroyServer(String serverId) {
        logger.info("destroying server {}",serverId);

    }
    @Override
    public List<ServerNode> createServers(int numOfServers) {
        logger.info("creating servers {}", numOfServers);
        return null;
    }
    @Override
    public void close() {
        logger.info("closing");
    }
    @Override
    public ServerNode bootstrapCloud( ServerNode serverNode ){
    	logger.info("bootstrapping cloud with [user,pass] = [{},{}]", serverNode.getProject(), serverNode.getKey() );
    	return null;
    	
    }

    @Override
    public List<ServerNode> recoverUnmonitoredMachines()
    {
        logger.info( "recovering lost machines" );
        return null;
    }

    @Override
    public BootstrapValidationResult validateBootstrap( ServerNode serverNode )
    {
        return new BootstrapValidationResult();
    }
}
