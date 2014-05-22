package beans.scripts;

import java.io.File;
import java.io.IOException;
import java.util.*;

import models.ServerNode;

import models.WidgetInstanceUserDetails;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;

import org.jclouds.compute.ComputeServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.ApplicationContext;
import utils.StringUtils;

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
//	 * @param cloudBootstrapConfiguration
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
		
		Map<String,Object> map = new HashMap<String, Object>();
		map.put( IS_HANDLE_PRIVATE_KEY_PROPERTY, String.valueOf( isHandlePrivateKey ) );
		map.put( CLOUD_FOLDER_PROPERTY, cloudFolder.getAbsolutePath() );
        map.put("action","bootstrap");
		addCommonProps(cmdLine, map, serverNode);

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
//	 * @param server
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
		
		Map<String,Object> map = new HashMap<String, Object>();
		addCommonProps( cmdLine, map, serverNode );
        map.put("action","install");
		
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

    private static void addCommonProps( CommandLine cmdLine, Map<String,Object> map, ServerNode serverNode ){
	
    	Map<String, String> environment = 
				ApplicationContext.get().conf().server.environment.getEnvironment();

		String executable = cmdLine.getExecutable();
		String[] arguments = cmdLine.getArguments();
		
		map.put( CMD_EXECUTABLE, executable );
		map.put( CMD_ARGUMENTS, StringUtils.join(arguments, ",") );
    	map.put( SERVER_NODE_ID_PROPERTY, String.valueOf( serverNode.getId() ) );
		map.put( CLOUDIFY_HOME_PROPERTY, environment.get( CLOUDIFY_HOME ) );


        // add application name and service for mail sending

        if ( serverNode.getWidget().sendEmail ) {

            map.put("serviceName", serverNode.getWidget().getConsoleUrlService());
            map.put("applicationName", serverNode.getWidget().getRecipeName());
            map.put("sendEmail", serverNode.getWidget().sendEmail);
            map.put("managerIp", serverNode.getPublicIP());

            Map<String,Object> mandrillDetails = new HashMap<String, Object>();
            map.put("mandril", mandrillDetails);


            mandrillDetails.put("apiKey", serverNode.getWidget().mandrillDetails.apiKey );
            mandrillDetails.put("templateName", serverNode.getWidget().mandrillDetails.templateName );

            List<MandrillDataItem> items = new LinkedList<MandrillDataItem>();
            items.add(new MandrillDataItem("name", serverNode.widgetInstanceUserDetails.name + " " + serverNode.widgetInstanceUserDetails.lastName));
            items.add(new MandrillDataItem("firstName", serverNode.widgetInstanceUserDetails.name ) );
            items.add(new MandrillDataItem("lastName", serverNode.widgetInstanceUserDetails.lastName));
            items.add(new MandrillDataItem("link", serverNode.getWidget().getConsoleURL()));
            items.add(new MandrillDataItem("linkTitle", serverNode.getWidget().getConsoleName()));

            mandrillDetails.put("data", items);

            List<MandrilEmailAddressItem> emailItems = new LinkedList<MandrilEmailAddressItem>();

            emailItems.add( new MandrilEmailAddressItem(serverNode.widgetInstanceUserDetails));
            String csvBccEmails = serverNode.getWidget().mandrillDetails.csvBccEmails;
            if ( !StringUtils.isEmptyOrSpaces(csvBccEmails) ){
                for (String item : csvBccEmails.split(",")) {
                    if ( !StringUtils.isEmptyOrSpaces(item)){
                        emailItems.add(new MandrilEmailAddressItem(item, "bcc address", "bcc"));
                    }
                }
            }


            mandrillDetails.put("to",emailItems);
        }
    }

    public static class MandrillDataItem{
        public String name;
        public String content;

        public MandrillDataItem(String name, String content) {
            this.name = name;
            this.content = content;
        }
    }

    public static class MandrilEmailAddressItem{
        public String email;
        public String name;
        public String type;

        public MandrilEmailAddressItem(String email, String name, String type) {
            this.email = email;
            this.name = name;
            this.type = type;
        }

        public MandrilEmailAddressItem( WidgetInstanceUserDetails details) {
            email = details.email;
            name = details.name + " " + details.lastName;
            type = "to";
        }
    }

	@Override
	public String getOutput( ServerNode serverNode ) {
		
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