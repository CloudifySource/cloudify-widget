package controllers.compositions;

import models.User;
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
            if ( getUser(context) == null ){
                return badRequest("must be logged in");
            }
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return badRequest("internal error");
        }
        return delegate.call(context);
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