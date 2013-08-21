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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import beans.config.Conf;
import beans.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.ServerNode;
import server.ExpiredServersCollector;
import server.ServerPool;

import javax.inject.Inject;

/**
 * This class schedules to delete the expired servers.
 * On delete bootstrap new machine and add to a server-pool.
 * 
 * @author Igor Goldenberg
 * @see ServerPoolImpl
 */
public class ExpiredServersCollectorImpl extends Timer implements ExpiredServersCollector
{
    @Inject
    private ServerPool serverPool;

    @Inject
    private Conf conf;

    private static Logger logger = LoggerFactory.getLogger( ExpiredServersCollectorImpl.class );


    /** a repeating time task in interval of @{link conf#destroyTickInterval}
     *
     ***/
    public void scheduleToDestroyTicker(){
         if ( conf.server.destroyMethod == ServerConfig.DestroyMethod.INTERVAL ){
            schedule( new DestroyServersTask(), 0, TimeUnit.MILLISECONDS.convert( 1 , TimeUnit.MINUTES ) );
         }
    }

    public static class DestroyServersTask extends TimerTask{
        @Override
        public void run() {
            logger.info("looking for servers to stop");

        }
    }

    public static class DestroyServerTask extends TimerTask{
        ServerNode server;
        final ServerPool serverPool;

        public DestroyServerTask(ServerNode server, ServerPool serverPool ) {
            this.server = server;
            this.serverPool = serverPool;
        }

        @Override
        public void run() {
            if (server.getNodeId() == null) { // possible if remote bootstrap that failed for some reason.
                server.delete();
            }

            logger.info("This server {} was scheduled for destroy after: {} ms", server, server.getElapsedTime());
            try {
                logger.info("scheduled destruction activated for {}", server.getNodeId());
                serverPool.destroy(server);
            } catch (Exception e) {
                logger.error("destroying server threw exception", e);
            }
        }
    }



    public void scheduleToDestroy(final ServerNode server) {
        if (conf.server.destroyMethod == ServerConfig.DestroyMethod.SCHEDULE) {
            schedule(new DestroyServerTask( server, serverPool ), server.getElapsedTime());
        }
    }

    @Override
    public void cancel() {
        logger.error("ExpiredServersCollectorImpl - timer cancelled", new Exception());
        super.cancel();
    }

    public void setConf( Conf conf )
    {
        this.conf = conf;
    }



    public void setServerPool( ServerPool serverPool )
    {
        this.serverPool = serverPool;
    }
}
