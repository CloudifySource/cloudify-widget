package controllers.compositions;

import models.User;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/12/13
 * Time: 2:32 PM
 */
public class UserCheck extends Action.Simple {
    private static Logger logger = LoggerFactory.getLogger(UserCheck.class);

    @Override
    public Result call(Http.Context context) throws Throwable {
        try {
            User user = null;
            if ( ( user = getUser(context) ) == null ){
                return badRequest("must be logged in");
            }else{
                String userId= getUserId( context );
                if ( userId != null && !userId.equals(user.getId().toString()) ){
                    return badRequest("userId does not match authToken");
                }
                context.args.put("user", user);
            }
        } catch (Exception e) {
            logger.warn( "error while checking user", e );
            return badRequest("internal error");
        }
        return delegate.call(context);
    }

    // this is definitely the ugliest code I've ever written.
    // play 2.0 does not have an API to extract path parameters..
    // https://groups.google.com/forum/#!topic/play-framework/sNFeqmd-mBQ
    // why should they? right?
    // so I am going to count on a pattern in the rest URLs.
    // if I have /user/ and then a number - I am going to refer to it as userId
    private String getUserId( Http.Context context ){
        String[] args = context.request().path().split("/");
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ( "user".equals(arg) && args.length > i + 1 && NumberUtils.isNumber( args[i+1])){
                return args[i+1];
            }

        }
        return null;
    }

    public User getUser( Http.Context context ){
        String[] authTokens = context.request().queryString().get("authToken");
        String authToken = null;
        if (authTokens != null && authTokens.length > 0) {
            authToken = authTokens[0];
        } else {
            Http.Cookie authTokenCookie = context.request().cookies().get("authToken");
            if (authTokenCookie != null) {
                authToken = authTokenCookie.value();
            }
        }

        if (authToken == null) {
            return null;
        }

        return models.User.validateAuthToken(authToken, true, context);
    }
}