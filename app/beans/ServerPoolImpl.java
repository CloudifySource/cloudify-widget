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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import akka.util.Duration;
import beans.config.Conf;

import beans.pool.PoolEvent;
import com.avaje.ebean.Ebean;
import models.ServerNode;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Akka;
import server.*;
import utils.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.OptimisticLockException;


/**
 * This class manages a server pool of available/busy bootstrapped machines(servers).
 * A server pool initialize with configured minimum/maximum number of servers. 
 * The get() method returns a bootstrap server instance.
 * On init() a server-pool deletes an expires or orphans servers. 
 * It also provides ability to startup with a cold init which deletes a running server.
 * 
 * @author Igor Goldenberg
 * @see ServerBootstrapperImpl
 */
public class ServerPoolImpl implements ServerPool
{

    private static Logger logger = LoggerFactory.getLogger( ServerPoolImpl.class );
    @Inject
    private ServerBootstrapper serverBootstrapper;


    @Inject
    private Conf conf;


    private static Predicate nonRemotePredicate = new Predicate() {
        @Override
        public boolean evaluate( Object o )
        {
            return !((ServerNode)o).isRemote();
        }
    };
    private static Predicate busyServerPredicate = new Predicate() {
        @Override
        public boolean evaluate(Object o) {
            return ((ServerNode)o).isBusy() && nonRemotePredicate.evaluate( o );
        }
    };

    private static Predicate nonBusyServerPredicate = new Predicate() {
        @Override
        public boolean evaluate(Object o) {
            return !((ServerNode)o).isBusy() && nonRemotePredicate.evaluate( o );
        }
    };

    private static Predicate failedBootstrapsPredicate = new Predicate() {
        @Override
        public boolean evaluate(Object o) {
            return ((ServerNode)o).getNodeId() == null;
        }
    };

    /**
     *
     * @param pool - the pool we need to clean
     * @return - a clean pool
     */
    private List<ServerNode> cleanPool( Collection<ServerNode> pool ){

        if ( CollectionUtils.isEmpty( pool )){
            return new LinkedList<ServerNode>(  );
        }

        List<ServerNode> cleanPool = new LinkedList<ServerNode>(  );
        for ( ServerNode serverNode : pool ) {
            BootstrapValidationResult bootstrapValidationResult = serverBootstrapper.validateBootstrap( serverNode );
            if ( !bootstrapValidationResult.isValid( ) ){
                    logger.info( "found a bad bootstrap on server [{}]. The test result showed the following [{}]. I should destroy this server..", serverNode, bootstrapValidationResult );
                    destroy( serverNode );
            }else{
                logger.info( "Found a working management server [{}], adding to clean pool", serverNode );
                cleanPool.add( serverNode );
            }
        }
        return cleanPool;
    }

	@Override
    public void init()
	{

        logger.info( "recovering lost machines" );
        List<ServerNode> lostMachines = serverBootstrapper.recoverUnmonitoredMachines();
        if ( !CollectionUtils.isEmpty( lostMachines )){
            logger.info( "found [{}] lost machines [{}]", CollectionUtils.size( lostMachines ), lostMachines );
            Ebean.save( lostMachines );
        }else{
            logger.info( "no lost machines found" );
        }

        logger.info( "Started to initialize ServerPool, cold-init={}", conf.server.pool.coldInit );
		// get all available running servers
        List<ServerNode> servers = ServerNode.all();

        Collection<ServerNode> busyServer = CollectionUtils.select( servers, busyServerPredicate );
        logger.info("I found {} busy servers", CollectionUtils.size(busyServer));


        Collection<ServerNode> availableServer = CollectionUtils.select( servers, nonBusyServerPredicate );
        availableServer = cleanPool( availableServer );
        logger.info(" I have {} available server, I need a minimum of {} and maximum of {}", new Object[]{ CollectionUtils.size(availableServer), conf.server.pool.minNode, conf.server.pool.maxNodes} );
		// create new servers if need
		if ( CollectionUtils.size( availableServer )  < conf.server.pool.minNode )
		{
			int serversToInit = conf.server.pool.minNode - CollectionUtils.size( availableServer );
            logger.info("creating {} new Servers", serversToInit);
            logger.info( "ServerPool starting to initialize {} servers...", serversToInit );
            addNewServerToPool( serversToInit );
            // remove servers if we have too much
		} else if ( CollectionUtils.size(availableServer) > conf.server.pool.maxNodes ){
            int i =0;
            int serversToDelete = CollectionUtils.size(availableServer) - conf.server.pool.maxNodes ;
            logger.info("deleting {} servers",serversToDelete);
            for (ServerNode server : availableServer) {
                if ( i >= serversToDelete){
                    break;
                }
                i++;
                destroy( server );
            }
        }

        // failed bootstraps.
        Collection<ServerNode> failedBootstraps =CollectionUtils.select( servers,  failedBootstrapsPredicate );
        if (!CollectionUtils.isEmpty(failedBootstraps)) {
            logger.info("deleting {} failed bootstraps : {}", CollectionUtils.size( failedBootstraps) ,failedBootstraps);
            Ebean.delete(failedBootstraps);
        }
    }

    @Override
    public Collection<ServerNode> getPool() {
        return ServerNode.findByCriteria(new ServerNode.QueryConf().setMaxRows(-1).criteria().setBusy(null).setRemote(false).done());
    }

    public ServerNodesPoolStats getStats(){
        ServerNodesPoolStats stats = new ServerNodesPoolStats();

        List<ServerNode> all = ServerNode.all();
        stats.all = CollectionUtils.size( all );
        stats.nonRemote = CollectionUtils.size(  CollectionUtils.select( all, nonRemotePredicate ) );
        stats.busyServers = CollectionUtils.size( CollectionUtils.select( all, busyServerPredicate ) );
        stats.nonBusyServers = CollectionUtils.size( CollectionUtils.select( all, nonBusyServerPredicate ) );
        stats.minLimit = conf.server.pool.minNode;
        stats.maxLimit = conf.server.pool.maxNodes;
        return stats;
    }
	
	/** @return a ServerNode from the pool, otherwise <code>null</code> if no free server available */
    @Override
	synchronized public ServerNode get(  )
	{
        logger.info( "getting a server node" );

        List<ServerNode> freeServers = ServerNode.findByCriteria(new ServerNode.QueryConf().setMaxRows(10).criteria().setBusy(false).setRemote(false).done());
        ServerNode selectedServer = null;
        if ( !CollectionUtils.isEmpty( freeServers )){
            for ( ServerNode freeServer : freeServers ) {
                if ( tryToGetFreeServer( freeServer )){
                    logger.info( "successfully got a free server [{}]", freeServer );
                    selectedServer = freeServer;
                    break;
                }
            }
        }else{
            logger.info( "freeServers is empty, adding a new server. pool status is [{}]", getStats() );
        }

		addNewServerToPool( NoOpCallback.instance );

		return selectedServer;
	}

    /**
     * <p>
     * This method checks the serverNode is accessible.<br/>
     * If the serverNode is accessible it will try to mark it as "busy" which will effectively
     * get it out of the pool.<br/>
     * </p>
     *
     * @param serverNode - the server node we are trying to get
     * @return - true iff successfully got the serverNode
     */
    private boolean tryToGetFreeServer( ServerNode serverNode )
    {
        try{
            BootstrapValidationResult result = serverBootstrapper.validateBootstrap( serverNode );
            if ( result.isValid() ) {
                serverNode.setBusySince( System.currentTimeMillis() );
                serverNode.save(); // optimistic locking
                return true;
            } else {
                logger.info( "serverNode[{}] has an invalid bootstrap. I will rebuild it.", result );
                rebuild( serverNode );
            }
        }catch(OptimisticLockException e){
            logger.info( "server [{}] already caught by another thread, could not get it", serverNode );
        }
        return false;
    }

    // this will destroy one machine and create another.
    public void rebuild( final ServerNode serverNode ){
        logger.info("rebuilding machine [{}]", serverNode);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if ( !serverBootstrapper.reboot(serverNode) ){
                    logger.info("could not rebuild machine [{}]. I will destroy it and create another one", serverNode );
                    destroy(serverNode);
                }else{
                    logger.info("machine [{}] rebuilt successfully", serverNode );
                    destroy(serverNode);
                    serverBootstrapper.createServers( 1 );
                }

            }
        }).start();

    }

    public void destroy( ServerNode serverNode )
	{
        if ( serverNode == null ){
            return; // nothing to do.
        }
        logger.info("destroying server {}", serverNode);

        // guy - removing "addNewServerToPool" - this is the destroy function, not create function.
        // guy - removing WidgetInstance.delete since we cascade removal
        // guy - removing ServerNode.delete, since we established it does not exist.
        serverBootstrapper.destroyServer( serverNode );
	}

    private void addNewServerToPool( int number ){
        for (int i = 0; i < number; i++) {
            addNewServerToPool( NoOpCallback.instance );
        }
    }

    private boolean isPoolSaturated(){
        return ServerNode.count() >= conf.server.pool.maxNodes;
    }

    @Override
    public void addNewServerToPool( final Runnable callback ) {
        logger.info("adding new server to the pool");
        Akka.system().scheduler().scheduleOnce(Duration.Zero(),
                new Runnable() {
                    public void run() {
                        try {
                            List<ServerNode> servers = serverBootstrapper.createServers(1);
                            for (ServerNode srv : servers) {
                                srv.save();
                            }
                        } catch (Exception e) {
                            logger.error("ServerPool failed to create a new server node", e);
                            String stackTrace = ExceptionUtils.getFullStackTrace(e);
                            ApplicationContext.get().getPoolEventManager().handleEvent(new PoolEvent.MachineStateEvent()
                                    .setType(PoolEvent.Type.ERROR)
                                    .setErrorMessage(e.getMessage())
                                    .setErrorStackTrace(stackTrace));
                        }
                        Akka.system().scheduler().scheduleOnce( Duration.Zero() , callback );
                    }
                }
        );
    }

    public void setServerBootstrapper(server.ServerBootstrapper serverBootstrapper) {
        this.serverBootstrapper = serverBootstrapper;
    }

    public void setConf( Conf conf )
    {
        this.conf = conf;
    }
}