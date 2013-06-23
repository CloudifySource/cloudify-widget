package controllers;

import beans.NovaCloudCredentials;
import beans.ServerBootstrapperImpl;
import beans.ServerNodesPoolStats;
import controllers.compositions.AdminUserCheck;
import controllers.compositions.UserCheck;
import models.*;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.*;
import server.ApplicationContext;
import server.ServerBootstrapper;
import views.html.widgets.dashboard.serverNodePool;

import java.util.List;

import static utils.RestUtils.resultAsJson;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/12/13
 * Time: 10:48 AM
 *
 *
 * NOT IMPLEMENTED YET! Still pending proper design
 * This will allow users to get an overlay of the widgets information over their machines. <br/>
 * The flow should be as follows :
 *
 * <li> User enters the tenant credentials</li>
 * <li> User is lead to a view where we show the machines this tenant has and the widgets installed on them</li>
 * <li> On each machine we should show which tenant installed the widget - assuming each tenant can view the others.</li>
 */


@With(UserCheck.class)
public class UserPoolController extends Controller {
    
    private static Logger logger = LoggerFactory.getLogger(UserPoolController.class);




    @With(UserCheck.class)
    public static Result getAllServers( String authToken, String project, String key, String secretKey )
    {
        ServerBootstrapper serverBootstrapper = ApplicationContext.get().getServerBootstrapper();
        List<Server> servers = serverBootstrapper.getAllMachines( new NovaCloudCredentials().setProject(project).setKey(key).setSecretKey(secretKey));
        for (Server server : servers) {

        }
        List<ServerNode> list = ServerNode.find.all();
        logger.debug("list of server nodes:\n{}", list);
        return ok( Json.toJson(list) );
    }


    @With(AdminUserCheck.class)
    public static Result getServerNodePoolPage(){
        return ok( serverNodePool.render() );
    }

    @With(UserCheck.class)
    public static Result summary( String authToken )
    {
        User user = User.validateAuthToken(authToken);

        Summary summary = new Summary();

        // only for admin users, we return summary information
            int totalUsers = User.find.findRowCount();
            summary.addAttribute( "Users", String.valueOf( totalUsers ) );

            int totalWidgets = Widget.find.findRowCount();

            // find only widget instances deployed on my cloud.
            int localInstances = WidgetInstance.find.where().eq( "serverNode.remote", false ).findRowCount();


            summary.addAttribute( "Widgets", String.valueOf( totalWidgets ));
            summary.addAttribute("Instances", String.valueOf( localInstances ));

            ServerNodesPoolStats stats = ApplicationContext.get().getServerPool().getStats();
            summary.addAttribute( "Idle Servers", String.valueOf( stats.nonBusyServers ));
            summary.addAttribute( "Busy Servers", String.valueOf( stats.busyServers ));


        return resultAsJson(summary);
    }
}
