package beans.scripts;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import models.ServerNode;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jclouds.compute.ComputeServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.ApplicationContext;

public class FileBasedScriptExecutor implements ScriptExecutor, ScriptExecutorsConstants{
	
/*    @Inject
    private Conf conf;*/
	
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
							boolean isHandlePrivateKey ) {

		String executable = cmdLine.getExecutable();
		String[] arguments = cmdLine.getArguments();
		
		String serverNodeId = String.valueOf(  serverNode.getId() );
		if( logger.isDebugEnabled() ){
			logger.debug( 
				"executable:" + executable + ", arguments=" + Arrays.toString( arguments ) + 
				", Environment=" + ApplicationContext.get().conf().server.environment.getEnvironment() +
				", serverNode.id=" + serverNodeId  );
		}
		
		Map<String,String> map = new HashMap<String, String>();
		map.put( IS_HANDLE_PRIVATE_KEY_PROPERTY, String.valueOf( isHandlePrivateKey ) );
		map.put( CLOUD_FOLDER_PROPERTY, cloudFolder.getAbsolutePath() );
		addCommonProps( cmdLine, map, serverNode );

		try {
			ScriptFilesUtilities.writeToJsonFile( serverNodeId, BOOTSTRAP, map );
			ScriptFilesUtilities.waitForFinishBootstrappingAndSaveServerNode( serverNode, cloudFolder, 
					jCloudsContext, isHandlePrivateKey );			
		} 
		catch (IOException e) {
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
    	
    	String serverNodeId = String.valueOf( serverNode.getId() );
    	if( logger.isDebugEnabled() ){
    		logger.debug( "Run script, command executable:" + cmdLine.getExecutable() + 
    				", arguments:" + Arrays.toString( cmdLine.getArguments() ) +
    				", Environment=" +  ApplicationContext.get().conf().server.environment.getEnvironment() +
    				", serverNode.id=" + serverNodeId +
    				", serverNode.getSecretKey=" + serverNode.getSecretKey() );
    	}
		
		Map<String,String> map = new HashMap<String, String>();
		addCommonProps( cmdLine, map, serverNode );
		
		try {
			ScriptFilesUtilities.writeToJsonFile( serverNodeId, INSTALL, map );
//			ScriptFilesUtilities.waitForFinishInstallationAndMoveFolderToExecuted( serverNodeId );
		} 
		catch (IOException e) {
			logger.error( e.toString(), e );
		}
    }

    @Override
    public void runTearDownCommand(CommandLine cmdLine) {

    }

    private static void addCommonProps( CommandLine cmdLine, Map<String,String> map, ServerNode serverNode ){
	
    	Map<String, String> environment = 
				ApplicationContext.get().conf().server.environment.getEnvironment();

		String executable = cmdLine.getExecutable();
		String[] arguments = cmdLine.getArguments();
		
		map.put( CMD_EXECUTABLE, executable );
		map.put( CMD_ARGUMENTS, StringUtils.join( arguments, "," ) );
    	map.put( SERVER_NODE_ID_PROPERTY, String.valueOf( serverNode.getId() ) );
		map.put( CLOUDIFY_HOME_PROPERTY, environment.get( CLOUDIFY_HOME ) );
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