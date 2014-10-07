package controllers.checkers;

import cloudify.widget.allclouds.executiondata.ExecutionDataModel;
import controllers.GsController;
import models.ServerNode;
import models.User;
import models.Widget;
import models.WidgetInstance;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import play.mvc.Result;
import server.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/11/14
 * Time: 10:44 AM
 */
public class CheckInstallFinishedEmailSendController extends GsController {

    public static Result testInstallFinishedEmail( Long widgetId ){
        User user = validateSession();
        if ( request().body() == null ){
            return internalServerError("data required but missing");
        }

        JsonNode jsonNode = request().body().asJson();
        if ( jsonNode == null ){
            return internalServerError("data required but missing ");
        }
        if ( !jsonNode.has("email")){
            return internalServerError("email required but missing");
        }

        if ( !jsonNode.has("name")){
            return internalServerError("name required but missing");
        }


        String email = jsonNode.get("email").getTextValue();
        String name = jsonNode.get("name").getTextValue();

        try {

            Widget widget = Widget.findByUserAndId(user, widgetId);
            ServerNode mockServerNode = new ServerNode();
            mockServerNode.setPublicIP("1.1.1.1");

            ExecutionDataModel.LoginDetails loginDetails = new ExecutionDataModel.LoginDetails();
            loginDetails.email = email;
            loginDetails.name = name;
            loginDetails.lastName = "mock last name";
            loginDetails.userId = "mockUserId";

            Map<String, ExecutionDataModel.LoginDetails> jsonMap = new HashMap<String, ExecutionDataModel.LoginDetails>();
            jsonMap.put("loginDetails", loginDetails);

            String executionDataStr = new ObjectMapper().writeValueAsString(jsonMap);
            jsonMap.put("loginDetails", loginDetails);


            ExecutionDataModel model = ApplicationContext.get().getNewExecutionDataModel();
            model.setRaw(executionDataStr);
            model.setEncryptionKey(ApplicationContext.get().conf().applicationSecret);
            mockServerNode.setExecutionData(model.encrypt());

            mockServerNode.setRandomPassword("some_random_value_mock");

            WidgetInstance widgetInstance = new WidgetInstance();
            widgetInstance.setWidget(widget);
            widgetInstance.setServerNode(mockServerNode);
            mockServerNode.setWidgetInstance(widgetInstance);

            ApplicationContext.get().getWidgetInstallFinishedSender().send(widget, mockServerNode);
        }catch(Exception e){
            return internalServerError(e.toString());
        }
        return ok();
    }
}
