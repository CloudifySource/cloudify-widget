package controllers;

import play.libs.Json;
import play.libs.OpenID;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * User: guym
 * Date: 1/25/13
 * Time: 4:50 PM
 */
public class Demos extends Controller {

    public static Result loginWithGoogle(){
        // http://stackoverflow.com/questions/9753702/openid-authentication-with-google-failing-randomly
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("email", "http://schema.openid.net/contact/email");
        String s = OpenID.redirectURL(
                "https://www.google.com/accounts/o8/id",
                routes.Demos.googleLoginCallback().absoluteURL( request() ),
                attributes
        ).get();
        return redirect(s);
    }

    public static Result googleLoginCallback(){
        OpenID.UserInfo userInfo = OpenID.verifiedId().get();
        return ok( views.html.demos.loginResult.render(userInfo.id.split("id=")[1], userInfo.attributes.get("email")));
    }

    public static Result validateUserIdFromLogin( String userId ){
        if ( "error".equals(userId )){
            return badRequest();
        }
        return ok();
    }

    public static Result getLoginDemoPage(){
        return ok(views.html.demos.login.render());
    }
}
