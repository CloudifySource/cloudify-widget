package beans.tasks;

import akka.util.Duration;
import models.ServerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import play.libs.Akka;
import server.ServerPool;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/22/13
 * Time: 3:06 PM
 *
 *
 * This task runs periodically and finds server we need to destroy.
 * Currently we are also using it to make sure the server pool is in good shape - meaning we have enough servers.
 *
 *
 */
public class DestroyServersTaskImpl implements DestroyServersTask {

    private static Logger logger = LoggerFactory.getLogger(DestroyServersTaskImpl.class);

    @Autowired private ServerPool serverPool;

    @Override
    public void run() {
        logger.debug("running DestroyServersTaskImpl");
        List<ServerNode> all = ServerNode.findByCriteria( new ServerNode.QueryConf().criteria().setRemote(false).done() );
        for (ServerNode serverNode : all) {
            logger.debug("checking to see if server [{}] expired", serverNode.toDebugString());
            if (serverNode.isExpired()) {
                DestroySingleServerTask task = new DestroySingleServerTask().setServerNode(serverNode).setServerPool(serverPool);
                Akka.system().scheduler().scheduleOnce(Duration.create(0, TimeUnit.MILLISECONDS), task);
            } else {
                logger.debug("not expired, timeleft is : " + serverNode.getTimeLeft());
            }
        }

        serverPool.runHealthCheck();
    }

    public void setServerPool(ServerPool serverPool) {
        this.serverPool = serverPool;
    }


    public static class DestroySingleServerTask implements Runnable{
        private ServerNode serverNode;
        private ServerPool serverPool;

        @Override
        public void run() {
             try{

                 if (serverNode == null ){
                     logger.info("DestroySingleServerTask got a NULL serverNode!", new RuntimeException());
                     return;
                 }

                 if ( serverPool == null ){
                     throw new  RuntimeException("must have a serverPool");
                 }

                 if ( serverNode.getNodeId() == null) { // possible if remote bootstrap that failed for some reason.
                     serverNode.delete();
                 }

                 try {
                     logger.info("destroying [{}]", serverNode );
                     serverPool.destroy(serverNode);
                 } catch (Exception e) {
                     logger.error("destroying server threw exception", e);
                 }

             }catch(Exception e){
                 logger.error("unable to run DestroySingleServerTask", e);
             }
        }

        public DestroySingleServerTask setServerNode( ServerNode serverNode ){
            this.serverNode = serverNode;
            return this;
        }

        public DestroySingleServerTask setServerPool(ServerPool serverPool) {
            this.serverPool = serverPool;
            return this;
        }
    }
}
