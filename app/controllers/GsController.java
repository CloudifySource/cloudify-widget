package controllers;

import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/3/14
 * Time: 12:27 PM
 */
public class GsController extends Controller{

    public static String GS_AUTH_TOKEN="AccountUuid";

    private static Logger logger = LoggerFactory.getLogger(GsController.class);

    public static User validateSession( boolean silent ){
        String authToken = session("authToken");


        try {
            if ( request().getHeader(GS_AUTH_TOKEN) != null ) {
                authToken = request().getHeader(GS_AUTH_TOKEN);
            }
        }catch(Exception e){
            logger.warn("error while reading auth token from ",e);
        }
        return User.validateAuthToken(authToken,silent);
    }


    public static User validateSession(  ){
        return validateSession( false );
    }

}
