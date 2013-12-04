package beans.scripts;

import java.io.File;

public interface ScriptExecutorsConstants {
	
	String SERVER_NODE_ID_PROPERTY = "serverNodeId";
	String CMD_LINE_PROPERTY = "cmdLine";
	String IS_HANDLE_PRIVATE_PROPERTY = "handlePrivateKey";
	String CLOUDIFY_HOME_PROPERTY = "cloudifyHome";
	String BOOTSTRAPPING_IN_PROGRESS_PROPERTY = "bootstrappingInProgress";
	String ADVANCED_PARAMS_PROPERTY = "advancedparams";
	String PUBLIC_IP_PROPERTY = "publicIp";
	String PRIVATE_IP_PROPERTY = "privateIp";
	
	String CLOUDIFY_HOME = "CLOUDIFY_HOME";
	
	String SCRIPTS_FOLDER_PATH = "." + File.separator + "_scripts";
	String NEW_SCRIPTS_FOLDER_PATH = SCRIPTS_FOLDER_PATH + File.separator + "new" + File.separator;
	String EXECUTING_SCRIPTS_FOLDER_PATH = SCRIPTS_FOLDER_PATH + File.separator + "executing" + File.separator;
	String EXECUTED_SCRIPTS_FOLDER_PATH = SCRIPTS_FOLDER_PATH + File.separator + "executed" + File.separator;
	String OUTPUT_FILE_NAME_PREFIX = "output-nodeid-";
		
	String SERVER_NODE_ID_DELIMETER= "_";
	String BOOTSTRAP = "bootstrap";
	String INSTALL = "install";
	String UNINSTALL = "uninstall";
	
	//String BOOTSTRAPPING_STATUS_FILE_NAME = "bootstrapping_status.json";
	String ERROR_MESSAGE_PROPERTY = "exception";
	String EXIT_STATUS_PROPERTY = "exitStatus";
}