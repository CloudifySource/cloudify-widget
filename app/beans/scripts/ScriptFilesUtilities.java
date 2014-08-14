package beans.scripts;

import cloudify.widget.common.CloudifyOutputUtils;
import cloudify.widget.common.asyncscriptexecutor.AsyncExecutionStatus;
import cloudify.widget.common.asyncscriptexecutor.IAsyncExecution;
import models.ServerNode;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.i18n.Messages;
import server.ApplicationContext;
import server.exceptions.ServerException;

public class ScriptFilesUtilities {
	

	
	private static Logger logger = LoggerFactory.getLogger( ScriptFilesUtilities.class );

	public static void waitForFinishBootstrappingAndSaveServerNode( ServerNode serverNode, IAsyncExecution asyncExecution){

		logger.info( "waiting for bootstrap status..." );


		while( !asyncExecution.isFinished() ){
			try{
				Thread.sleep(ApplicationContext.get().conf().asyncExecution.statusPollingIntervalMillis );
			} 
			catch (InterruptedException e) {
				logger.warn( "unable to sleep while polling async execution", e );
			}
		}

		logger.info( "bootstrap status found..." );

		try{
            AsyncExecutionStatus status = asyncExecution.getStatus();

            logger.info("exist code is ", status.exitCode);

			String output = asyncExecution.getOutput();

			if( status.exception != null ) {
				logger.info( "we have exceptions, checking for known issues" );

				if ( output.contains( "found existing management machines" ) ) {
					logger.info( "found 'found existing management machines' - issuing cloudify already exists message" );
					throw new ServerException( "i18n:cloudifyAlreadyExists" );
				}
				logger.info( "Command execution ended with errors: {}", output );

				throw new RuntimeException( "Failed to bootstrap cloudify machine: " + 
						status.exception + "\n" + output );
			}

			logger.info( "finished handling errors, extracting IP" );
			String publicIp = CloudifyOutputUtils.getBootstrapIp(output);
			if ( StringUtils.isEmpty( publicIp ) ) {
				logger.warn( "No public ip address found in bootstrap output. " + output );
				throw new RuntimeException( "Bootstrap failed. No IP address found in bootstrap output."
						+ output/*, executeException*/ );
			}
			logger.info( "ip is [{}], saving to serverNode. updating server node with new info. bootstrap cloud ended successfully", publicIp );

			serverNode.setPublicIP( publicIp );
			serverNode.save();

			logger.info("server node updated and saved");
		}
        catch( ServerException e ){
            throw e;
        }
		catch( Exception e ) {
			serverNode.errorEvent("i18n:invalidCredentials").save();
			throw new RuntimeException("Unable to bootstrap cloud", e);
		}
	}


}