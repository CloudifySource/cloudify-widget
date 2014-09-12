package controllers.checkers;

import beans.cloudbootstrap.AwsEc2CloudProviderCreator;
import cloudify.widget.ec2.Ec2ImageShare;
import controllers.GsController;

import models.User;
import models.Widget;
import org.codehaus.jackson.JsonNode;
import play.mvc.Result;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/11/14
 * Time: 10:43 AM
 */
public class CheckImageShareController extends GsController {

    public static Result testImageSharing( Long widgetId ){
        User user = validateSession();
        Widget widget = Widget.findByUserAndId(user, widgetId);

        if ( widget.getAwsImageShare() == null ){
            return badRequest("awsImage sharing details not saved on the widget. please save first.");
        }

        JsonNode jsonNode = request().body().asJson();

        if ( !jsonNode.has("accountId")){
            return badRequest("missing account id to share with");
        }

        String accountId = jsonNode.get("accountId").getTextValue();

        if ( !jsonNode.has("operation")){
            return badRequest("must choose what to do : add  or remove permission");
        }

        Ec2ImageShare.Operation operation = Ec2ImageShare.Operation.valueOf(jsonNode.get("operation").getTextValue());


        try {
            new AwsEc2CloudProviderCreator().shareImage(widget.getAwsImageShare(), accountId, operation);
        }catch(Exception e){
            return internalServerError(e.getMessage());
        }
        return ok("please check image was shared with account");
    }
}
