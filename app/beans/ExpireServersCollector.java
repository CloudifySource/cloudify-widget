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
import java.util.Timer;
import java.util.TimerTask;

import play.Logger;

import models.ServerNode;
import server.ApplicationContext;
import server.ExpiredServersCollector;

import javax.inject.Inject;

/**
 * This class schedules to delete the expired servers.
 * On delete bootstrap new machine and add to a server-pool.
 * 
 * @author Igor Goldenberg
 * @see beans.ServerPool
 */
public class ExpireServersCollector extends Timer implements ExpiredServersCollector
{
    @Inject
    private ServerPool serverPool;
	public void scheduleToDestroy(final ServerNode server)
	{
		server.setExpirationTime( System.currentTimeMillis() + SERVER_POOL_EXPIRATION_TIME );
		
		Logger.info( "This server " + server.getPublicIP() + " was scheduled for destroy after: " + server.getElapsedTime() + " ms");

		schedule(new TimerTask()
		{
			public void run()
			{
				serverPool.destroy(server.getId());
			}
			
		}, server.getElapsedTime() );
	}

    public void setServerPool(ServerPool serverPool) {
        this.serverPool = serverPool;
    }
}
