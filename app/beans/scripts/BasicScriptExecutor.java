package beans.scripts;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import models.ServerNode;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jclouds.compute.ComputeServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import beans.api.ExecutorFactory;
import beans.config.Conf;
import beans.config.ServerConfig.CloudBootstrapConfiguration;

import play.i18n.Messages;
import server.ApplicationContext;
import server.ProcExecutor;
import server.exceptions.ServerException;
import utils.CloudifyUtils;
import utils.Utils;

public class BasicScriptExecutor implements ScriptExecutor{
	
    @Inject
    private ExecutorFactory executorFactory;
    
    @Inject
    private Conf conf;    

	private static Logger logger = LoggerFactory.getLogger( BasicScriptExecutor.class );

	public BasicScriptExecutor(){
		logger.info( "---Initializing BasicScriptExecutor---" );
	}	
	
	/**
	 * Used for bootstrapping
	 * @param cmdLine
	 * @param serverNode
	 * @param jCloudsContext
	 * @param cloudFolder
	 * @param cloudBootstrapConfiguration
	 * @param isHandlePrivateKey
	 */
	@Override
	public void runBootstrapScript( CommandLine cmdLine, ServerNode serverNode, 
							ComputeServiceContext jCloudsContext, File cloudFolder,
							boolean isHandlePrivateKey ) {
		
		try{
			//Command line for bootstrapping remote cloud.
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
			ProcExecutor bootstrapExecutor = executorFactory.getBootstrapExecutor( serverNode );

			logger.info( "Executing command line: " + cmdLine );
			bootstrapExecutor.execute( cmdLine, ApplicationContext.get().conf().server.environment.getEnvironment(), resultHandler );
			logger.info( "waiting for output" );
			resultHandler.waitFor();
			logger.info( "finished waiting , exit value is [{}]", resultHandler.getExitValue() );
			logger.info( "> serverNode ID:" + serverNode.getId() );
			String cachedOutput = getOutput( serverNode );
			logger.info( "> cachedOutput:" + cachedOutput );
			String output = Utils.getOrDefault( cachedOutput, "" );
			logger.info( "> output:" + output );
			
			if ( resultHandler.getException() != null ) {
				logger.info( "we have exceptions, checking for known issues" );
				if ( output.contains( "found existing management machines" ) ) {
					logger.info( "found 'found existing management machines' - issuing cloudify already exists message" );
					throw new ServerException( Messages.get( "cloudify.already.exists" ) );
				}
				logger.info( "Command execution ended with errors: {}", output );
				throw new RuntimeException( "Failed to bootstrap cloudify machine: "
						+ output, resultHandler.getException() );
			}

			logger.info( "finished handling errors, extracting IP" );
			String publicIp = Utils.extractIpFromBootstrapOutput( output );
			if ( StringUtils.isEmpty( publicIp ) ) {
				logger.warn( "No public ip address found in bootstrap output. " + output );
				throw new RuntimeException( "Bootstrap failed. No IP address found in bootstrap output."
						+ output, resultHandler.getException() );
			}
			logger.info( "ip is [{}], saving to serverNode", publicIp );

			if( isHandlePrivateKey ){
				String privateKey = CloudifyUtils.getCloudPrivateKey( cloudFolder );
				if ( StringUtils.isEmpty( privateKey ) ) {
					throw new RuntimeException( "Bootstrap failed. No pem file found in cloud directory." );
				}
				logger.info( "found PEM string" );
				serverNode.setPrivateKey( privateKey );
			}
			
			logger.info( "Bootstrap cloud command ended successfully" );

			logger.info( "updating server node with new info" );
			serverNode.setPublicIP( publicIp );

			serverNode.save();
			logger.info("server node updated and saved");
		}
		catch( Exception e ) {
			serverNode.errorEvent("Invalid Credentials").save();
			throw new RuntimeException("Unable to bootstrap cloud", e);
		} 
		finally {
			CloudBootstrapConfiguration cloudBootstrapConfiguration = conf.server.cloudBootstrap;
			if( cloudFolder != null && cloudBootstrapConfiguration.removeCloudFolder ) {
				FileUtils.deleteQuietly( cloudFolder );
			}
			if (jCloudsContext != null) {
				jCloudsContext.close();
			}
			serverNode.setStopped(true);
		}
	}
	
	/**
	 * used for running install and uninstall of applications
	 * @param cmdLine
	 * @param server
	 */
    @Override
	public void runInstallationManagementScript( CommandLine cmdLine, ServerNode server ){
    	
        try {
            ProcExecutor executor = executorFactory.getDeployExecutor( server );
            ExecuteResultHandler resultHandler = executorFactory.getResultHandler(cmdLine.toString());
            logger.info( "executing command [{}]", cmdLine );
            executor.execute( cmdLine, 
            	ApplicationContext.get().conf().server.environment.getEnvironment(), resultHandler );
            logger.info( "The process instanceId: {}", executor.getId() );
        } 
        catch ( ExecuteException e ) {
            logger.error( "Failed to execute process. Exit value: " + e.getExitValue(), e );

            throw new ServerException( "Failed to execute process. Exit value: " + e.getExitValue(), e );
        } 
        catch ( IOException e ) {
            logger.error( "Failed to execute process", e );

            throw new ServerException( "Failed to execute process.", e );
        }
    }

	@Override
	public String getOutput( ServerNode serverNode ) {
		return Utils.getCachedOutput( serverNode );
	}
}