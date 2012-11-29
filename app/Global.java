import models.User;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import server.ApplicationContext;
import server.Config;

/**
 * On system startup trigger event onStart or onStop.
 * 
 * @author Igor Goldenberg
 */
public class Global extends GlobalSettings
{
	@Override
	public void onStart(Application app)
	{
		// print cloudify configuration
		Logger.info( Config.print() );
		
	    ApplicationContext.getServerPool();
		
		// create Admin user if not exists
		if ( User.find.where().eq("email", Config.ADMIN_USERNAME).findUnique() == null )
		{
			User adminUser = User.newUser("Cloudify", "Administrator", Config.ADMIN_USERNAME, Config.ADMIN_PASSWORD);
			adminUser.setAdmin(true);
		    adminUser.save();
		}
	}

	@Override
	public void onStop(Application app)
	{
		ApplicationContext.getServerBootstrapper().close();
	}
}
