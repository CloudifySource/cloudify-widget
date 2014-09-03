package controllers;

import models.User;
import play.mvc.Controller;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/3/14
 * Time: 12:27 PM
 */
public class GsController extends Controller{

    public static User validateSession(){
        String authToken = session("authToken");
        return User.validateAuthToken(authToken);
    }

}
