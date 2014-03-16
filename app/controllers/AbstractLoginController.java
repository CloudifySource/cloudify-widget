package controllers;

import cloudify.widget.api.clouds.IWidgetLoginDetails;
import cloudify.widget.api.clouds.IWidgetLoginHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import server.ApplicationContext;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 3/16/14
 * Time: 11:41 PM
 */
public abstract class AbstractLoginController extends Controller {

    private static Logger logger = LoggerFactory.getLogger(AbstractLoginController.class);

    public static void handleLogin( IWidgetLoginDetails loginDetails ){
        Collection<IWidgetLoginHandler> loginHandlers = ApplicationContext.get().getLoginHandlers();
        for (IWidgetLoginHandler loginHandler : loginHandlers) {
            try {
                loginHandler.handleWidgetLogin( loginDetails );
            } catch (Exception e) {
                logger.error("unable to handle login [{}]", loginHandler.getClass(), e);
            }
        }
    }

}
