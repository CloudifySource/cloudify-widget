package beans.scripts;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import models.ServerNode;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.jclouds.compute.ComputeServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.i18n.Messages;
import play.libs.Json;
import server.ApplicationContext;
import server.exceptions.ServerException;
import utils.CloudifyUtils;
import utils.Utils;
import beans.api.ExecutorFactory;
import beans.config.ServerConfig.CloudBootstrapConfiguration;

public class FileBasedScriptExecutor implements ScriptExecutor, ScriptExecutorsConstants{
	
    @Inject
    private ExecutorFactory executorFactory;
	
	private static Logger logger = LoggerFactory.getLogger( FileBasedScriptExecutor.class );
	
	public FileBasedScriptExecutor(){
		logger.info( "---Initializing FileBasedScriptExecutor---" );
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
							CloudBootstrapConfiguration cloudBootstrapConfiguration, 
							boolean isHandlePrivateKey ) {

		String commandLine = cmdLine.toString();
		String serverNodeId = String.valueOf(  serverNode.getId() );
		Map<String,String> environment = ApplicationContext.get().conf().server.environment.getEnvironment();
		logger.info( "commandLine:" + commandLine + ", isHandlePrivateKey=" + isHandlePrivateKey + 
				", Environment=" + environment + ", serverNode.id=" + serverNodeId +
				", serverNode.getSecretKey=" + serverNode.getSecretKey() );
		
		Map<String,String> map = new HashMap<String, String>();
		map.put( CMD_LINE_PROPERTY, commandLine );
		map.put( IS_HANDLE_PRIVATE_PROPERTY, String.valueOf( isHandlePrivateKey ) );
		map.put( CLOUDIFY_HOME_PROPERTY, environment.get( CLOUDIFY_HOME ) );
		addCommonProps( map, serverNode );

		writeToJsonFile( serverNodeId, BOOTSTRAP, map );

		logger.info( "waiting for status..." );

		while( !isBootstrappingFinished( serverNodeId, serverNodeId ) ){
			try{
				Thread.sleep( 3* 1000 );
			} 
			catch (InterruptedException e) {
				logger.warn( e.toString(), e );
			}
		}
		
		logger.info( "status found..." );

		try{
/*
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();


			ProcExecutor bootstrapExecutor = executorFactory.getBootstrapExecutor( serverNode );

			logger.info( "Executing command line: " + cmdLine );
			//Command line for bootstrapping remote cloud.
			bootstrapExecutor.execute( cmdLine, ApplicationContext.get().conf().server.environment.getEnvironment(), resultHandler );
*/
//			resultHandler.waitFor();
			logger.info( "finished waiting , exit value is [{}]", 
					getBootstrappingExitStatus( serverNodeId, serverNodeId ) );
			logger.info( ">>> serverNodeId=" + serverNode.getId() );
			String cachedOutput = Utils.getCachedOutput( serverNode );
			logger.info( ">>> cachedOutput=" + cachedOutput );
			String output = Utils.getOrDefault( cachedOutput, "" );
			logger.info( ">>> output=" + output );

			String executeException = 
					getBootstrapErrorMessage( serverNodeId, serverNodeId );
			logger.info( ">>> Bootstrap execution output=" + output );

			if( executeException != null ) {
				logger.info( "we have exceptions, checking for known issues" );
				if ( output.contains( "found existing management machines" ) ) {
					logger.info( "found 'found existing management machines' - issuing cloudify already exists message" );
					throw new ServerException( Messages.get( "cloudify.already.exists" ) );
				}
				logger.info( "Command execution ended with errors: {}", output );
				throw new RuntimeException( "Failed to bootstrap cloudify machine: " + 
							executeException + "\n" + output );
			}

			logger.info( "finished handling errors, extracting IP" );
			String publicIp = Utils.extractIpFromBootstrapOutput( output );
			if ( StringUtils.isEmpty( publicIp ) ) {
				logger.warn( "No public ip address found in bootstrap output. " + output );
				throw new RuntimeException( "Bootstrap failed. No IP address found in bootstrap output."
						+ output/*, executeException*/ );
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
			if( cloudFolder != null && cloudBootstrapConfiguration.removeCloudFolder ) {
				FileUtils.deleteQuietly( cloudFolder );
			}
			if (jCloudsContext != null) {
				jCloudsContext.close();
			}
			serverNode.setStopped(true);
		}
	}

	private static int getBootstrappingExitStatus( String subFolderName, String serverNodeId ) {

		int retValue = -1;
		
		try {
			JsonNode parsedJson = getStatusJson( subFolderName, BOOTSTRAP, serverNodeId );
			if( parsedJson != null ){
				JsonNode statusJsonNode = parsedJson.get( EXIT_STATUS_PROPERTY );
//				JsonNode errorMessageJsonNode = parsedJson.get( ERROR_MESSAGE_PROPERTY );
				if( statusJsonNode != null ){
					retValue = statusJsonNode.getIntValue();
				}
			}
		} 
		catch (IOException e) {
			logger.error( e.toString(), e );
		}
		
		return retValue;
	}	
	
	private static boolean isBootstrappingFinished( String subFolderName, String serverNodeId ) {

		boolean retValue = false;

		try {
			if( logger.isDebugEnabled() ){
				logger.debug( "---START isBootstrappingFinished()" );
			}
			JsonNode parsedJson = getStatusJson( subFolderName, BOOTSTRAP, serverNodeId );
			if( logger.isDebugEnabled() ){
				logger.debug( "---isBootstrappingFinished(), parsedJson=" + parsedJson );
			}
			if( parsedJson != null ){
				JsonNode statusJsonNode = parsedJson.get( EXIT_STATUS_PROPERTY );
				if( logger.isDebugEnabled() ){
					logger.debug( "---isBootstrappingFinished(), statusJsonNode=" + statusJsonNode );
				}
				if( statusJsonNode != null ){
					logger.info( "Exit code:" + statusJsonNode.getIntValue() );
					retValue = true;
				}
			}
		} 
		catch (IOException e) {
			logger.error( e.toString(), e );
		}

		return retValue;
	}
	
	private static JsonNode getStatusJson( String subFolderName, String opName, String serverNodeId ) throws IOException {

		JsonNode retValue = null;
	
		String path = EXECUTING_SCRIPTS_FOLDER_PATH +  
				subFolderName + File.separator + serverNodeId + SERVER_NODE_ID_DELIMETER + opName + "_status.json";
		File resultJsonFile = FileUtils.getFile( path );
		if( logger.isDebugEnabled() ){
			logger.debug( "---getStatusJson(), statusJsonNodeFile=" + path + ", exists:" + resultJsonFile.exists() );
		}
		
		if( !resultJsonFile.exists() ){
			return null;
		}
		
		String fileContent = FileUtils.readFileToString( resultJsonFile );
		if( logger.isDebugEnabled() ){
			logger.debug( "---getStatusJson(), fileContent=" + fileContent  );
		}
		retValue = Json.parse( fileContent );
		
		return retValue;
	}	

	private static void writeToJsonFile( String serverNodeId, String opName, Map<String, String> map ) {

		JsonNode json = Json.toJson( map );
		
		logger.info( "Created json:" + json );
		
		File scriptsFolder = new File( NEW_SCRIPTS_FOLDER_PATH );
		if( !scriptsFolder.exists() ){
			scriptsFolder.mkdirs();
		}
		
		File jsonFile = new File( scriptsFolder.getPath() + 
					File.separator + serverNodeId + SERVER_NODE_ID_DELIMETER + opName + ".json" );
		try {
			FileUtils.write( jsonFile, json.toString() );
		} 
		catch( IOException e ){
			logger.error( e.toString(), e );
		}
	}

	/**
	 * used for running install and uninstall of applications
	 * @param cmdLine
	 * @param server
	 */
    @Override
	public void runInstallationManagementScript( CommandLine cmdLine, ServerNode serverNode ){
    	
    	String commandLine = cmdLine.toString();
    	Map<String, String> environment = 
    						ApplicationContext.get().conf().server.environment.getEnvironment();
		logger.info( "Run script, commandLine:" + commandLine + ", Environment=" +  environment +
				", serverNode.id=" + serverNode.getId()
				+ ", serverNode.getSecretKey=" + serverNode.getSecretKey() );
		
		Map<String,String> map = new HashMap<String, String>();
		map.put( CMD_LINE_PROPERTY, commandLine );
		map.put( CLOUDIFY_HOME_PROPERTY, environment.get( CLOUDIFY_HOME ) );
		addCommonProps( map, serverNode );
		
		writeToJsonFile( String.valueOf( serverNode.getId() ), INSTALL, map );
    	
    	/*
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
        }*/
    }
    
    private static void addCommonProps( Map<String,String> map, ServerNode serverNode ){
	
    	map.put( SERVER_NODE_ID_PROPERTY, String.valueOf( serverNode.getId() ) );
		map.put( ADVANCED_PARAMS_PROPERTY, String.valueOf( serverNode.getAdvancedParams() ) );
		if( serverNode.getPublicIP() != null ){
			map.put( PUBLIC_IP_PROPERTY, String.valueOf( serverNode.getPublicIP() ) );
		}
		if( serverNode.getPrivateIP() != null ){
			map.put( PRIVATE_IP_PROPERTY, String.valueOf( serverNode.getPrivateIP() ) );
		}
    }

	@Override
	public String getOutput( ServerNode serverNode ) {
		
		long nodeId = serverNode.getId();
		String outputFileName = EXECUTING_SCRIPTS_FOLDER_PATH +  
					nodeId + File.separator + OUTPUT_FILE_NAME_PREFIX + nodeId + ".log";
		
		if( logger.isDebugEnabled() ){
			logger.debug( "> outputFileName=" + outputFileName );
		}
		
		File outputFile = new File( outputFileName );
		if( logger.isDebugEnabled() ){
			logger.debug( "> output File exists=" + outputFile.exists() );
		}		
		
		if( !outputFile.exists() ){
			return "";
		}
		
		String retValue;
		try {
			retValue = FileUtils.readFileToString( outputFile );
		} 
		catch( IOException e ) {
			logger.warn( e.toString(), e );
			retValue = "";
		}
		
		return retValue;
	}
	
	private static String getBootstrapErrorMessage( String subFolderName, String serverNodeId ) {

		String retValue = null;
		try {
			JsonNode parsedJson = getStatusJson( subFolderName, BOOTSTRAP, serverNodeId );
			if( parsedJson != null ){
				JsonNode errorMessageJsonNode = parsedJson.get( ERROR_MESSAGE_PROPERTY );
				if( errorMessageJsonNode != null ){
					retValue = errorMessageJsonNode.getTextValue();
				}
			}
		} 
		catch (IOException e) {
			logger.error( e.toString(), e );
		}
		
		return retValue;
	}	
}