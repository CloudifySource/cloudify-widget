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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import akka.util.Duration;
import beans.config.Conf;


import cloudify.widget.allclouds.executiondata.ExecutionDataModel;
import com.avaje.ebean.Ebean;
import models.ServerNode;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Akka;
import server.*;
import utils.CollectionUtils;
import utils.StringUtils;

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

    // counts the machines that are undergoing "create" process.
    // we need to count them as well when we want to know if pool is missing resources or not.
    private AtomicInteger undergoingBootstrapCount = new AtomicInteger(0);


    @Inject
    private Conf conf;


    private static Predicate nonRemotePredicate = new Predicate() {
        @Override
        public boolean evaluate( Object o )
        {
            ServerNode node = (ServerNode) o;
            boolean hasAdvancedParams = node.getExecutionDataModel().has(ExecutionDataModel.JsonKeys.ADVANCED_DATA);
            boolean result = !node.isRemote() && !hasAdvancedParams;
            logger.debug("server [{}] is remote [{}] advanced params [{}] result [{}]", node.getId(), node.isRemote() , hasAdvancedParams, result );
            return result;
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
            return ((ServerNode)o).getNodeId() == null && nonRemotePredicate.evaluate(o);
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
                logger.info( "Found a working management server [{}]:[{}], adding to clean pool", serverNode.getNodeId(), serverNode.getId() );
                cleanPool.add( serverNode );
            }
        }
        return cleanPool;
    }

    @Override
    public void runHealthCheck() {
        if ( !isPoolWillBeSaturated() ){
            logger.debug("healthcheck :: creating more instance [{}]", getStats() );
            addNewServerToPool(1); // lets create just one.
        } else {
            logger.debug("healthcheck :: not creating more instances [{}]", getStats() );
        }
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
        logger.info("investigating [{}] servers", CollectionUtils.size(servers));
        Collection<ServerNode> busyServer = CollectionUtils.select( servers, busyServerPredicate );
        logger.info("I found {} busy servers", CollectionUtils.size(busyServer));


        Collection<ServerNode> availableServer = CollectionUtils.select( servers, nonBusyServerPredicate );
        availableServer = cleanPool( availableServer );
        logger.info(" I have {} available servers, I need a minimum of {} and maximum of {}", new Object[]{ CollectionUtils.size(availableServer), conf.server.pool.minNode, conf.server.pool.maxNodes} );
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
        Collection<ServerNode> failedBootstraps = new HashSet<ServerNode>(CollectionUtils.select( servers,  failedBootstrapsPredicate ));
        logger.info("found [{}] failed bootstraps", CollectionUtils.size(failedBootstraps));
        Collection<Long> failedIds = new HashSet<Long>();
        for (ServerNode sn : failedBootstraps   ) {
            failedIds.add( sn.getId() );
        }

        if ( CollectionUtils.size( failedIds) != CollectionUtils.size(failedBootstraps)){
            logger.error("ERROR : duplicate failed bootstrap machines! need to fix query. [{}] unique, [{}] total", CollectionUtils.size(failedIds), CollectionUtils.size(failedBootstraps));
        }

        logger.info("deleting {} failed bootstraps : {}", CollectionUtils.size( failedIds ) ,failedIds);
        if (!CollectionUtils.isEmpty(failedBootstraps)) {

            try{
                Ebean.delete(failedBootstraps);
            }catch(RuntimeException e){
                logger.info("unable to delete all server nodes with failed bootstraps. iterating");
                for (ServerNode failedBootstrap : failedBootstraps) {
                    try{
                        failedBootstrap.refresh();
                        failedBootstrap.delete();
                    }catch(RuntimeException e1){
                        logger.info("unable to delete server [{}]", failedBootstrap.getId(),e1);
                    }
                }
            }
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
        stats.undergoingBootstrap = undergoingBootstrapCount.get();
        return stats;
    }
	
	/** @return a ServerNode from the pool, otherwise <code>null</code> if no free server available */
    @Override
	synchronized public ServerNode get(  )
	{
        logger.info( "getting a server node" );

        printStats();

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

    public void printStats(){
        new Thread( new PrintStats(this)).start();
    }

    public static class PrintStats implements Runnable{
        ServerPoolImpl poolImpl;

        public PrintStats(ServerPoolImpl poolImpl) {
            this.poolImpl = poolImpl;
        }

        @Override
        public void run() {
            logger.info("pool stats [{}]",poolImpl.getStats());
        }
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


    // this will destroy one machine and if necessary will create another.
    public void rebuild( final ServerNode serverNode ){
        logger.info("rebuilding machine [{}]", serverNode);
        new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("destroying machine [{}]", serverNode);
                serverBootstrapper.destroyServer(serverNode);
                addNewServerToPool(1);
//                if ( !isPoolWillBeSaturated() ){ // we have enough machines. just kill it
//                    logger.info("pool is not saturated. we will create another machine");
//                    serverBootstrapper.createServers(1);
//                }
            }
        }).start();

    }

    // this function also counts the machine undergoing creation.
    // it tells if after those machines' bootstrap the pool will be saturated.
    public boolean isPoolWillBeSaturated(){
        return getStats().nonBusyServers + undergoingBootstrapCount.get() >= conf.server.pool.maxNodes;
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
        return getStats().nonBusyServers >= conf.server.pool.maxNodes;
    }

    @Override
    public void addNewServerToPool( final Runnable callback ) {
        logger.info("adding new server to the pool");
//        if ( isPoolSaturated() ){
//            logger.error("pool is saturated and someone asked for more machines", new RuntimeException());
//        }
        Akka.system().scheduler().scheduleOnce(Duration.Zero(),
                new Runnable() {
                    public void run() {
                        try {
                            if ( !isPoolWillBeSaturated() ){


                                undergoingBootstrapCount.addAndGet(1);
                                logger.info("creating new :: undergoing bootstrap count [{}]", getStats() );

                                List<ServerNode> servers = serverBootstrapper.createServers(1);
                                // fixing this.
                                undergoingBootstrapCount.decrementAndGet();
                                for (ServerNode srv : servers) {
                                    srv.save();

                                    logger.info("after create :: undergoing bootstrap count [{}]", getStats());
                                }
                            }
                            else {
                                logger.info("not creating :: stats [{}] .", getStats());
                            }
                        } catch (Exception e) {
                            logger.error("ServerPool failed to create a new server node", e);
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

    @Override
    public void clearPool() {
        int limit = 1000;
        ServerNode serverNode = null;

        // the idea behind this approach is that we immediately open a request to create a new server for every
        // server we get from pool.
        // otherwise, the request to create new servers is delayed until we delete all server, and that might take a long time.

        while( (serverNode = get()) != null && limit > 0){
            limit --;
            serverNode.setBusySince( System.currentTimeMillis());
            serverNode.setStopped( true );
            serverNode.save();
        }

        // this is another way to go at it.. but does not give as good results as the above.

//        List<ServerNode> all = ServerNode.all();
//        Collection<ServerNode> select = CollectionUtils.select(all, nonBusyServerPredicate);
//        logger.info("found [{}] nodes in the pool", CollectionUtils.size(select));
//        for (ServerNode serverNode : select) {
//
//        }
//        Ebean.save(select);

        logger.info("cleaned pool successfully");
    }

    public Object getPoolNodesByStatus(){
        Map<String,Collection> nodes = new HashMap<String, Collection>();
        List<ServerNode> all = ServerNode.all();
        nodes.put("free", convert(CollectionUtils.select(all, nonBusyServerPredicate)));
        nodes.put("occupied", convert(CollectionUtils.select(all, busyServerPredicate)));
        return nodes;
    }


    public Collection<PoolNode> convert( Collection<ServerNode> coll ){
        Collection<PoolNode> result = new LinkedList<PoolNode>();

        for (ServerNode item : coll) {
            result.add(new PoolNode(item));
        }

        return result;
    }

    public static class PoolNode{
        public String ip;
        public Long timeLeft;
        public String id;
        public String modelId;
        public boolean stopped;

        public PoolNode( ServerNode serverNode ){
            ip = serverNode.getPublicIP();
            timeLeft = serverNode.getTimeLeft();
            id = serverNode.getNodeId();
            modelId = serverNode.getId().toString();
            stopped = serverNode.isStopped();
        }
    }
}