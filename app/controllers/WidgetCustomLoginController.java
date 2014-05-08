package controllers;

import cloudify.widget.common.MailChimpWidgetLoginHandler;
import models.Widget;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.validation.Constraints;
import play.mvc.Result;
import server.HeaderMessage;
import utils.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 4/10/14
 * Time: 1:41 PM
 */
public class WidgetCustomLoginController extends AbstractLoginController{

    private static Logger logger = LoggerFactory.getLogger(WidgetCustomLoginController.class);

    public static Result customLogin(   String widgetKey ){
        if ( StringUtils.isEmptyOrSpaces(widgetKey) ){
            return internalServerError("missing widgetId");
        }
        Widget widget = Widget.getWidget(widgetKey);
        logger.info("logged in custom");
        JsonNode jsonNode = request().body().asJson();
        logger.info("jsonNode is : " + jsonNode);

        try {
            handleLogin( widget, new CustomLoginDetails(jsonNode));
        }catch(CustomLoginException e){
            return internalServerError(e.getMessage());
        }catch( Exception e ){
            logger.info("custom login failed",e);
            return internalServerError("invalid details");
        }



        return ok();
    }

    public static class CustomLoginException extends RuntimeException{
        public CustomLoginException() {
            super();
        }

        public CustomLoginException(String message) {
            super(message);
        }

        public CustomLoginException(String message, Throwable cause) {
            super(message, cause);
        }

        public CustomLoginException(Throwable cause) {
            super(cause);
        }
    }

    public static class CustomLoginDetails implements MailChimpWidgetLoginHandler.MailChimpLoginDetails{
        public String email;
        public String firstName;
        public String lastName;

        public CustomLoginDetails( JsonNode jsonNode ){
            if ( !jsonNode.has("name")){
                throw new CustomLoginException("name is required");
            }

            if ( !jsonNode.has("lastName")){
                throw new CustomLoginException("last name is required");
            }

            if ( !jsonNode.has("email")){
                throw new CustomLoginException("email is required");
            }
            firstName = jsonNode.get("name").getTextValue();
            lastName = jsonNode.get("lastName").getTextValue();
            email = jsonNode.get("email").getTextValue();

            Constraints.EmailValidator ev =  new Constraints.EmailValidator();
            if ( StringUtils.isEmpty(email) || !ev.isValid( email ) ){
                new HeaderMessage().setError( "Email is incorrect" ).apply( response().getHeaders() );
                throw new CustomLoginException("invalid email address :: " + email );
            }
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
