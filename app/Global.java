import beans.config.Conf;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Application;
import play.GlobalSettings;
import play.libs.Json;
import server.ApplicationContext;

/**
 * On system startup trigger event onStart or onStop.
 * 
 * @author Igor Goldenberg
 */
public class Global extends GlobalSettings
{
    private static Logger logger = LoggerFactory.getLogger( Global.class );
	@Override
	public void onStart(Application app)
	{
		// print cloudify configuration
        Conf conf = ApplicationContext.conf();

        logger.info( Json.stringify( Json.toJson( conf ) ) );

	    ApplicationContext.getServerPool();

        // create Admin user if not exists
		if ( User.find.where().eq("admin", Boolean.TRUE ).findRowCount() <= 0 )
		{
            logger.info( "no admin user. creating from configuration" );
			User adminUser = User.newUser("Cloudify", "Administrator",
                    conf.server.admin.username,
                    conf.server.admin.password );
			adminUser.setAdmin(true);
		    adminUser.save();
		}else{
            logger.info( "found admin user" );
        }
	}

	@Override
	public void onStop(Application app)
	{
		ApplicationContext.getServerBootstrapper().close();
	}
}
