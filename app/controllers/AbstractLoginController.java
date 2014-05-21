package controllers;

import cloudify.widget.api.clouds.IWidgetLoginDetails;
import cloudify.widget.common.MailChimpWidgetLoginHandler;
import models.Widget;
import models.WidgetInstanceUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Controller;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 3/16/14
 * Time: 11:41 PM
 */
public abstract class AbstractLoginController extends Controller {

    private static Logger logger = LoggerFactory.getLogger(AbstractLoginController.class);

    public static void handleLogin( Widget widget, IWidgetLoginDetails loginDetails ){

        if (loginDetails instanceof MailChimpWidgetLoginHandler.MailChimpLoginDetails) {
            MailChimpWidgetLoginHandler.MailChimpLoginDetails details = (MailChimpWidgetLoginHandler.MailChimpLoginDetails) loginDetails;

            WidgetInstanceUserDetails userDetails = new WidgetInstanceUserDetails();
            userDetails.setLastName( details.getLastName() );
            userDetails.setName( details.getFirstName() );
            userDetails.setEmail( details.getEmail() );

            session().put(WidgetInstanceUserDetails.COOKIE_NAME,Json.toJson(userDetails).toString());

        }


        if ( widget.mailChimpDetails != null && widget.mailChimpDetails.isEnabled() ){
            logger.info("I got mailchimp details. I will send to it : "  + widget.mailChimpDetails.toString() );
            try{
                MailChimpWidgetLoginHandler mclh = new MailChimpWidgetLoginHandler();
                mclh.setListId(widget.mailChimpDetails.getListId());
                mclh.setApiKey(widget.mailChimpDetails.getApiKey());
                mclh.handleWidgetLogin(loginDetails);
            }catch(Exception e){
                logger.error("error while sending login details to mailchimp",e);
            }
        }else{
            logger.info("no mailchimp details");
        }

    }

}
