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

import java.util.Timer;
import java.util.TimerTask;

import beans.config.Conf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.ServerNode;
import server.ExpiredServersCollector;

import javax.inject.Inject;

/**
 * This class schedules to delete the expired servers.
 * On delete bootstrap new machine and add to a server-pool.
 * 
 * @author Igor Goldenberg
 * @see ServerPoolImpl
 */
public class ExpireServersCollectorImpl extends Timer implements ExpiredServersCollector
{
    @Inject
    private ServerPoolImpl serverPool;

    @Inject
    private Conf conf;

    private static Logger logger = LoggerFactory.getLogger( ExpireServersCollectorImpl.class );

	public void scheduleToDestroy(final ServerNode server)
	{
		server.setExpirationTime( System.currentTimeMillis() + conf.server.pool.expirationTimeMillis );
		
		logger.info( String.format( "This server %s was scheduled for destroy after: %d ms", server.getPublicIP(), server.getElapsedTime() ) );

		schedule(new TimerTask()
		{
			public void run()
			{
				serverPool.destroy(server.getId());
			}
			
		}, server.getElapsedTime() );
	}

    public void setServerPool(ServerPoolImpl serverPool) {
        this.serverPool = serverPool;
    }

    public void setConf( Conf conf )
    {
        this.conf = conf;
    }
}
