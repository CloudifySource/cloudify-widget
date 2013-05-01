/*
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
 */
package beans;

import java.util.Timer;
import java.util.TimerTask;

import beans.config.Conf;
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

	public void scheduleToDestroy(final ServerNode server)
	{
        if ( server.getNodeId() == null ){ // possible if remote bootstrap that failed for some reason.
            server.delete();
        }
		
		logger.info( "This server {} was scheduled for destroy after: {} ms", server, server.getElapsedTime() );
		schedule( new TimerTask() {
            public void run()
            {
                try {
                    logger.info("scheduled destruction activated for {}", server.getNodeId());
                    serverPool.destroy( server );
                } catch ( Exception e ) {
                    logger.error("destroying server threw exception", e);
                }
            }

        }, server.getElapsedTime() );
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
