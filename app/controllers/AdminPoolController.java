package controllers;

import static utils.RestUtils.resultAsJson;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import models.ServerNode;
import models.Summary;
import models.User;
import models.Widget;
import models.WidgetInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clouds.base.CloudServer;

import play.libs.Akka;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.mvc.With;
import server.ApplicationContext;
import server.ServerBootstrapper;
import server.ServerPool;
import server.WidgetServer;
import utils.CollectionUtils;
import beans.BootstrapValidationResult;
import beans.NoOpCallback;
import beans.ServerNodesPoolStats;
import beans.config.Conf;
import beans.config.ServerConfig;
import controllers.compositions.AdminUserCheck;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/12/13
 * Time: 2:37 PM
 */
@With(AdminUserCheck.class)
public class AdminPoolController extends UserPoolController {
    private static Logger logger = LoggerFactory.getLogger(AdminPoolController.class);


    public static Result getWidgetInstances( String authToken ){
        return ok(Json.toJson(WidgetInstance.find.all()));
    }

    public static Result index(){
        return ok(views.html.widgets.dashboard.serverNodePool.render());
    }

    // todo : do a publish/subscribe pattern here and use web socket.
    public static Result getStatuses( String authToken ){

        // guy - bug in play 2 limits us here to use only synchronous results
        // this bug was fixed in 2.1.
        // read more : https://groups.google.com/forum/#!msg/play-framework/CuwRQ2V05AM/7r5JGfzrmOsJ

//        return async( Akka.future(new Callable<Result>() {
//            @Override
//            public Result call() throws Exception {
                List<ServerNode> serverNodes = ServerNode.findByCriteria(new ServerNode.QueryConf().setMaxRows(-1).criteria().setRemote(false).done());
                WidgetServer widgetServer = ApplicationContext.get().getWidgetServer();
                List<Widget.Status> statuses = new LinkedList<Widget.Status>();
                for (ServerNode serverNode : serverNodes) {
                    try {
                        statuses.add(widgetServer.getWidgetStatus(serverNode));
                    } catch (Exception e) {
                        logger.error("unable to get status for [{}]", serverNode);
                    }
                }
                return ok(Json.toJson(statuses));
//            }
//        }));

    }

    public static Result getServerNodes( String authToken ) {
        return ok( Json.toJson(ServerNode.findByCriteria(new ServerNode.QueryConf().setMaxRows(-1).criteria().setRemote(false).done())));
    }

    private static ServerNode getServerNodeSafely( String authToken, String nodeId ){
        User user = User.validateAuthToken( authToken );
        ServerNode serverNode = CollectionUtils.first(ServerNode.findByCriteria(new ServerNode.QueryConf().criteria().setUser(user).setNodeId(nodeId).done()));
        return serverNode;
    }

    public static Result checkAvailability( String authToken , String nodeId ){
        ServerBootstrapper serverBootstrapper = ApplicationContext.get().getServerBootstrapper();
        ServerNode serverNode = getServerNodeSafely(authToken, nodeId) ;
        BootstrapValidationResult bootstrapValidationResult = serverBootstrapper.validateBootstrap(serverNode);
        return ok( Json.toJson(bootstrapValidationResult));
    }

    public static Result getCloudServers( String authToken ) {

        return async(Akka.future(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                ServerBootstrapper serverBootstrapper = ApplicationContext.get().getServerBootstrapper();
                ServerPool serverPool = ApplicationContext.get().getServerPool();

                serverPool.getPool();
                Conf conf = ApplicationContext.get().conf();
                ServerConfig.ApiCredentials apiCredentials = conf.server.bootstrap.api;
                // get all machines with our tag.
                List<CloudServer> servers = serverBootstrapper.getAllMachines(ApplicationContext.getNovaCloudCredentials()
                        .setProject(apiCredentials.project)
                        .setKey(apiCredentials.key)
                        .setApiCredentials(false)
                        .setSecretKey(apiCredentials.secretKey)
                );
                return ok(Json.toJson(servers));
            }
        }));
    }


    public static Result summary( String authToken ) {
        Summary summary = new Summary();
        // only for admin users, we return summary information
        int totalUsers = User.find.findRowCount();
        summary.addAttribute("Users", String.valueOf(totalUsers));

        int totalWidgets = Widget.find.findRowCount();
        summary.addAttribute("Widgets", String.valueOf(totalWidgets));


        // find only widget instances deployed on my cloud.
        int localInstances = WidgetInstance.find.where().eq("serverNode.remote", false).findRowCount();
        summary.addAttribute("Instances", String.valueOf(localInstances));

        ServerNodesPoolStats stats = ApplicationContext.get().getServerPool().getStats();
        summary.addAttribute("Idle Servers", String.valueOf(stats.nonBusyServers));
        summary.addAttribute("Busy Servers", String.valueOf(stats.busyServers));


        return resultAsJson(summary);
    }


    public static Result removeNode( String authToken, String nodeId ){
        ServerNode serverNode = getServerNodeSafely( authToken, nodeId );
        if ( serverNode != null ){ // try to find it in the database
        ApplicationContext.get().getServerPool().destroy( serverNode );
        }else{ // handle machines that are not in our DB but exist in the cloud.
            ApplicationContext.get().getServerBootstrapper().deleteServer( nodeId );
        }
        return ok();
    }

    public static Result addNode( String authToken ){
        ApplicationContext.get().getServerPool().addNewServerToPool(NoOpCallback.instance);
        return ok();
    }


    public static WebSocket<String> poolEvents( final String authToken ) {
//        return new WebSocket<String>() {
//
//            // Called when the Websocket Handshake is done.
//            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
//                try{
//                try{Thread.sleep(2000);}catch(Exception e){}
//               new WebSocketEventListener()
//                       .setUser(User.validateAuthToken(authToken, true, null ))
//                       .setIn(in)
//                       .setManager( ApplicationContext.get().getPoolEventManager() )
//                       .setOut(out).listen();
//                }catch(Exception e){
//                    logger.error("unable to listen",e);
//                }
//
//            }
//
//        };
        return null; //todo implement better.
    }

}
