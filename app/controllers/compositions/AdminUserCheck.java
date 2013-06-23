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
 * Time: 2:05 PM
 * <p/>
 * checks if user is logged in and if the user is admin.
 * uses the request parameter by default, and falls back to cookie.
 */
public class AdminUserCheck extends Action.Simple {
    private static Logger logger = LoggerFactory.getLogger(AdminUserCheck.class);

    UserCheck userCheck = new UserCheck();

    @Override
    public Result call(Http.Context context) throws Throwable {
        try {
            User user = userCheck.getUser(context);
            if (user == null || !user.isAdmin()) {
                return badRequest("invalid user access");
            }
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return delegate.call(context);
    }
}


