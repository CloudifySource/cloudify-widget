package beans.scripts;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import models.ServerNode;

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
import beans.config.ServerConfig.CloudBootstrapConfiguration;

public class ScriptFilesUtilities implements ScriptExecutorsConstants{
	
	private final static long SLEEP_TIMEOUT = 6*10000;
	
	private static Logger logger = LoggerFactory.getLogger( ScriptFilesUtilities.class );

	public static void waitForFinishBootstrappingAndSaveServerNode( ServerNode serverNode, 
															ComputeServiceContext jCloudsContext ){

		String serverNodeId = String.valueOf( serverNode.getId() );
		try {
			//find bootstrap json file and retrieve from it cloud folder path and isHandlePrivateKey flag value
			JsonNode parsedJson = getJson( serverNodeId, BOOTSTRAP, serverNodeId );
			//if appropriate json file found
			if( parsedJson != null ){
				JsonNode cloudFolderJsonNode = parsedJson.get( CLOUD_FOLDER_PROPERTY );
				JsonNode isHandlePrivateKeyJsonNode = parsedJson.get( IS_HANDLE_PRIVATE_KEY_PROPERTY );

				File cloudFolder = new File( cloudFolderJsonNode.getTextValue() );
				boolean isHandlePrivateKey = isHandlePrivateKeyJsonNode.getBooleanValue();

				waitForFinishBootstrappingAndSaveServerNode( serverNode, cloudFolder, jCloudsContext,
						isHandlePrivateKey );
			}
		} 
		catch( IOException e ) {
			logger.error( e.toString(), e );
		}
	}

	public static void waitForFinishBootstrappingAndSaveServerNode( ServerNode serverNode, 
			File cloudFolder, ComputeServiceContext jCloudsContext, boolean isHandlePrivateKey ){

		logger.info( "waiting for bootstrap status..." );

		String serverNodeId = String.valueOf( serverNode.getId() );

		while( !isBootstrappingFinished( serverNodeId, serverNodeId ) ){
			try{
				Thread.sleep( SLEEP_TIMEOUT );
			} 
			catch (InterruptedException e) {
				logger.warn( e.toString(), e );
			}
		}

		logger.info( "bootstrap status found..." );

		try{
			logger.info( "finished waiting , exit value is [{}]", 
					getBootstrappingExitStatus( serverNodeId, serverNodeId ) );
			logger.info( ">>> serverNodeId=" + serverNode.getId() );
			String cachedOutput = getOutput( serverNode );
			logger.info( ">>> cachedOutput=" + cachedOutput );
			String output = Utils.getOrDefault( cachedOutput, "" );
			logger.info( ">>> output=" + output );

			String executeException = 
					getBootstrapErrorMessage( serverNodeId, serverNodeId );
			logger.info( ">>> executeException=" + executeException );

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
			CloudBootstrapConfiguration cloudBootstrapConfiguration = 
					ApplicationContext.get().conf().server.cloudBootstrap;
			if( cloudFolder != null && cloudBootstrapConfiguration.removeCloudFolder ) {
				FileUtils.deleteQuietly( cloudFolder );
			}
			if (jCloudsContext != null) {
				jCloudsContext.close();
			}
			serverNode.setStopped(true);
		}		
	}
	
	public static void writeToJsonFile( String serverNodeId, String opName, Map<String, Object> map ) throws IOException {

		JsonNode json = Json.toJson( map );
		
		logger.info( "Created json:" + json );
		
		File newScriptsFolder = new File( NEW_SCRIPTS_FOLDER_PATH );
		if( !newScriptsFolder.exists() ){
			newScriptsFolder.mkdirs();
		}
		
		File jsonFile = new File( 
				createOperationJsonFileName( newScriptsFolder.getPath(), serverNodeId, opName ) );
		
		FileUtils.write( jsonFile, json.toString() );
	}
	
	/*
	public static void waitForFinishInstallationAndMoveFolderToExecuted( String serverNodeId ) {

		logger.info( "waiting for install status..., serverNodeId=" + serverNodeId );

		while( !isInstallFinished( serverNodeId, serverNodeId ) ){
			try{
				Thread.sleep( SLEEP_TIMEOUT );
			} 
			catch (InterruptedException e) {
				logger.warn( e.toString(), e );
			}
		}

		logger.info( "install status found..." );		
		try {
			moveExecutingContentToExecuted( serverNodeId );
		} 
		catch( IOException e ){
			logger.error( e.toString(), e );
		}		
	}*/
	
	public static JsonNode getJson( String subFolderName, String opName, String serverNodeId ) throws IOException {

		JsonNode retValue = null;
	
		String path = createOperationJsonFileName( 
							EXECUTING_SCRIPTS_FOLDER_PATH + subFolderName, serverNodeId, opName ); 
		File resultJsonFile = FileUtils.getFile( path );
		if( logger.isDebugEnabled() ){
			logger.debug( "---getJson(), jsonNodeFile=" + path + ", exists:" + resultJsonFile.exists() + ", serverNodeId=" + serverNodeId );
		}
		
		if( !resultJsonFile.exists() ){
			return null;
		}
		
		String fileContent = FileUtils.readFileToString( resultJsonFile );
	    logger.info( " --- file exists --- getJson(), fileContent=" + fileContent  );
		retValue = Json.parse( fileContent );
		
		return retValue;
	}	
	
	public static JsonNode getStatusJson( String subFolderName, String opName, String serverNodeId ) throws IOException {

		JsonNode retValue = null;
	
		String path = createOperationStatusJsonFileName( 
							EXECUTING_SCRIPTS_FOLDER_PATH + serverNodeId, serverNodeId, opName );
		File resultJsonFile = FileUtils.getFile( path );
    	logger.info( "---getStatusJson(), statusJsonNodeFile=" + path + ", exists:" + resultJsonFile.exists() );

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

	public static String createOperationJsonFileName( 
								String parentFolderPath, String serverNodeId, String operationName ){  
		return parentFolderPath + File.separator + serverNodeId + SERVER_NODE_ID_DELIMETER + operationName + ".json";
	}
			
	public static String createOperationStatusJsonFileName( 
								String parentFolderPath, String serverNodeId, String operationName ){			
		return parentFolderPath + File.separator +"bootstrap.status";
	}	
	
	/*
	private static boolean isInstallFinished( String subFolderName, String serverNodeId ) {

		boolean retValue = false;

		try {
			if( logger.isDebugEnabled() ){
				logger.debug( "---START isInstallFinished()" );
			}
			JsonNode parsedJson = getStatusJson( subFolderName, INSTALL, serverNodeId );
			if( logger.isDebugEnabled() ){
				logger.debug( "---isInstallFinished(), parsedJson=" + parsedJson );
			}
			if( parsedJson != null ){
				JsonNode statusJsonNode = parsedJson.get( EXIT_STATUS_PROPERTY );
				if( logger.isDebugEnabled() ){
					logger.debug( "---isInstallFinished(), statusJsonNode=" + statusJsonNode );
				}
				if( statusJsonNode != null ){
					logger.info( "Install exit code:" + statusJsonNode.getIntValue() );
					retValue = true;
				}
			}
		} 
		catch (IOException e) {
			logger.error( e.toString(), e );
		}

		return retValue;
	}	
	
	
	private static void moveExecutingContentToExecuted( String nodeId ) throws IOException{
		
		String executingFolderName = EXECUTING_SCRIPTS_FOLDER_PATH + nodeId;
		File executedFolder = new File( EXECUTED_SCRIPTS_FOLDER_PATH );
		if( !executedFolder.exists() ){
			executedFolder.mkdirs();
		}
		
		FileUtils.moveDirectoryToDirectory( new File( executingFolderName ), executedFolder, false );
	}	*/
	
	public static boolean isBootstrappingFinished( String subFolderName, String serverNodeId ) {

		boolean retValue = false;

		try {
     		logger.debug( "---START isBootstrappingFinished()" );
			JsonNode parsedJson = getStatusJson( subFolderName, BOOTSTRAP, serverNodeId );


			if( parsedJson != null ){
                logger.info( "---isBootstrappingFinished(), parsedJson=" + parsedJson );
				JsonNode statusJsonNode = parsedJson.get( EXIT_STATUS_PROPERTY );
    			logger.info( "---isBootstrappingFinished(), statusJsonNode=" + statusJsonNode );
				if( statusJsonNode != null ){
					logger.info( "Bootstrap exit code:" + statusJsonNode.getIntValue() );
					retValue = true;
				}
			}
		} 
		catch (IOException e) {
			logger.error( e.toString(), e );
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

	private static String getOutput( ServerNode serverNode ) {
		
		long nodeId = serverNode.getId();
		String outputFileName = EXECUTING_SCRIPTS_FOLDER_PATH +  
					nodeId + File.separator + "output.log";
		
		if( logger.isDebugEnabled() ){
			logger.debug( "> outputFileName=" + outputFileName );
		}
		
		File outputFile = new File( outputFileName );
		if( logger.isDebugEnabled() ){
			logger.debug( "> output File exists=" + outputFile.exists() + 
					", absolute path:" + outputFile.getAbsolutePath() +
					", path:" + outputFile.getPath() );
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
}