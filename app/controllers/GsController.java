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

    public static User validateSession(){
        String authToken = session("authToken");


        try {
            if ( request().headers().containsKey(GS_AUTH_TOKEN)) {
                authToken = request().headers().get(GS_AUTH_TOKEN)[0];
            }
        }catch(Exception e){
            logger.warn("error while reading auth token from ",e);
        }
        return User.validateAuthToken(authToken,true);
    }

}
