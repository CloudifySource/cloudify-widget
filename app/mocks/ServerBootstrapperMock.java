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

package mocks;

import java.util.Collections;
import java.util.List;

import models.ServerNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.ServerBootstrapper;
import beans.BootstrapValidationResult;
import beans.NovaCloudCredentials;
import clouds.base.CloudServer;

public class ServerBootstrapperMock implements ServerBootstrapper 
{
    private static Logger logger = LoggerFactory.getLogger(ServerBootstrapperMock.class);

    @Override
    public void destroyServer( ServerNode serverNode )
    {
        logger.info( "destroying server [{}]", serverNode );
    }

    @Override
    public void deleteServer(String nodeId) {
        logger.info("destroying nodeId [{}]", nodeId );
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
        logger.info("validating bootstrap");
        return new BootstrapValidationResult();
    }

    @Override
    public List<CloudServer> getAllMachines(NovaCloudCredentials cloudCredentials) {
        logger.info("getting all machines");
        return Collections.emptyList();
    }

    @Override
    public boolean reboot(ServerNode serverNode) {
        logger.info("rebooting serverNode [{}]", serverNode );
        return true;
    }
}
