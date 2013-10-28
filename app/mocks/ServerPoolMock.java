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

import beans.ServerNodesPoolStats;
import models.ServerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.ServerPool;

import java.util.Collection;
import java.util.Collections;

public class ServerPoolMock implements ServerPool 
{
    private static Logger logger = LoggerFactory.getLogger( ServerPoolMock.class );

    @Override
    public void init()
    {

    }

    @Override
    public Collection<ServerNode> getPool() {
        logger.info("getting pool");
        return Collections.emptyList();
    }

    @Override
    public ServerNodesPoolStats getStats()
    {
        return new ServerNodesPoolStats();
    }

    public ServerNode get(  )
    {
        logger.info( "getting server node" );
        return null;
    }

    @Override
    public void destroy( ServerNode server )
    {
        logger.info( "destroying : {} " , server );

    }

    @Override
    public void addNewServerToPool( Runnable callback ) {
        logger.info("adding new server to pool");
        callback.run();

    }
}
