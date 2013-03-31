import beans.config.Conf;
import models.User;
import models.Widget;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.LoggerFactory;
import play.Application;
import play.GlobalSettings;
import play.api.Play;
import play.api.mvc.Results;
import play.api.mvc.SimpleResult;
import play.core.j.JavaResults;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import scala.Tuple2;
import scala.collection.JavaConversions;
import server.ApplicationContext;
import server.exceptions.ExceptionResponse;
import server.exceptions.ExceptionResponseDetails;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * On system startup trigger event onStart or onStop.
 * 
 * @author Igor Goldenberg
 */
public class Global extends GlobalSettings
{
    private static org.slf4j.Logger logger = LoggerFactory.getLogger( Global.class );

    private Conf conf;
	@Override
	public void onStart(Application app)
	{
		// print cloudify configuration
        conf = ApplicationContext.get().conf();
        logger.info( Json.stringify( Json.toJson( conf ) ) );

        // initialize the server pool.
        // letting the bean do it is incorrect..
        // TODO : this should open another thread.
        new Thread( new Runnable() { // guy - lets use this for now to fix #33
            @Override
            public void run()
            {
                ApplicationContext.get().getServerPool().init();
            }
        }).start();

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

        uploadInitialData( app );
	}

    private void uploadInitialData( Application app )
    {
        if ( !conf.settings.initialData.load ) {
            logger.info( "configuration set to not load initial data. skipping initial data" );
            return;
        }

        File file = app.getFile( "conf/initialData/initial-data.json" );

        try {
            ApplicationContext.get().getInitialData().load( FileUtils.readFileToString( file ) );

        } catch ( Exception e ) {
            logger.info( "unable to read initial data from : " + file );
        }
    }

    @Override
    public Result onError( Http.RequestHeader requestHeader, Throwable throwable )
    {

        logger.error( String.format( "experienced error {}", Utils.requestToString( requestHeader ) ), throwable );

        // todo : maybe this should be a method implemented in the exception.
        // I assume there is an easier way to do this, but this is what I have so far.
        // the code below simply detects this is our exception, and it sets headers so the GUI can respond accordingly.
        Results.Status status = JavaResults.InternalServerError();
        SimpleResult result;
        if ( throwable.getCause() != null && throwable.getCause() instanceof  ExceptionResponse ){ // customize response according to this exception
            ExceptionResponseDetails res = ( ( ExceptionResponse) throwable.getCause() ).getResponseDetails();
//
//
            Tuple2<String, String> ac = new Tuple2<String, String>( res.getHeaderKey(), res.toJson());
                    ArrayList<Tuple2<String, String>> list = new ArrayList<Tuple2<String, String>>();
                    list.add(ac);
                    scala.collection.immutable.List<Tuple2<String, String>> headers =
                      JavaConversions.asBuffer( list ).toList();
//
//
//            guy -- important.. even though Intellij marks this as error, it is not an error.. ignore it.
            status.header().headers().$plus(  ac );
           result = status.withHeaders( headers );
//            return result;
//            return play.mvc.Results.internalServerError();
        }else{
            return null;
        }

        final SimpleResult finalResult = result;
        return new Result() {
            @Override
            public play.api.mvc.Result getWrappedResult()
            {
                return finalResult;
            }
        };



    }




	@Override
	public void onStop(Application app)
	{
		ApplicationContext.get().getServerBootstrapper().close();
	}
}
