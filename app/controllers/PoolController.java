package controllers;

import cloudify.widget.api.clouds.CloudServer;
import cloudify.widget.api.clouds.CloudServerApi;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Result;
import server.ApplicationContext;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/14/14
 * Time: 6:44 PM
 */
public class PoolController extends GsController {

    private static Logger logger = LoggerFactory.getLogger(PoolController.class);

    public static Result getAllMachinesThatMatchPrefix(){
        validateSession();

        CloudServerApi cloudServerApi = ApplicationContext.get().getCloudServerApi();
        String tag = ApplicationContext.get().getConf().getServer().bootstrap.tag;

        Collection<CloudServer> allMachinesWithTag = cloudServerApi.getAllMachinesWithTag(tag);
        return ok(Json.toJson(allMachinesWithTag));
    }


    public static Result stopCloudNode( ){
        validateSession();
        JsonNode jsonNode = request().body().asJson();
        String nodeId = jsonNode.get("nodeId").getTextValue();


        CloudServerApi cloudServerApi = ApplicationContext.get().getCloudServerApi();
        cloudServerApi.delete(nodeId);

        return ok("stopped successfully");
    }
}
