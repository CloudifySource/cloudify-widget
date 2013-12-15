package beans.scripts;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import models.ServerNode;
import models.Widget;
import models.WidgetInstance;

import org.slf4j.LoggerFactory;

import play.libs.Akka;
import server.ApplicationContext;
import akka.util.Duration;

public class RestoreExecutionService implements ScriptExecutorsConstants {
	
	private static org.slf4j.Logger logger = LoggerFactory.getLogger( RestoreExecutionService.class );
	
	
    public static void restoreExecutions() {
    	
    	if( logger.isDebugEnabled() ){
    		logger.debug( "---START restoreExecutions" );
    	}

//    	restoreServerNodesBeforeBootstrap();
    	
    	restoreBootstrappingServerNodes();
    	
//    	restoreServerNodesAfterBootstrapAndInstallNotStarted();
//    	restoreServerNodesAfterInstalling();    			
	}

    /*
	private static void restoreServerNodesAfterInstalling() {
		//TODO
		File executingFolder = new File( EXECUTING_SCRIPTS_FOLDER_PATH );
		if( executingFolder.exists() ){
			File[] executingNodeIdsFolders = executingFolder.listFiles();
			for( File executingNodeIdFolder : executingNodeIdsFolders ){
				final String serverNodeId = executingNodeIdFolder.getName();
				String executingNodeIdFolderPath = executingNodeIdFolder.getPath();
				
				File installJsonFile = new File( ScriptFilesUtilities.createOperationJsonFileName( 
											executingNodeIdFolderPath, serverNodeId, INSTALL ) );
				File installJsonStatusFile = new File( 
										ScriptFilesUtilities.createOperationStatusJsonFileName( 
											executingNodeIdFolderPath, serverNodeId, INSTALL ) );
				
				if( logger.isDebugEnabled() ){
					logger.debug( "> serverNodeId=" + serverNodeId + 
						", executingNodeIdFolderPath=" + executingNodeIdFolderPath +
						", installJsonFile exists=" + installJsonFile.exists() + 
						", installJsonStatusFile exists:" + installJsonStatusFile.exists() );
				}
				//check if xxx_install.json exists
				if( installJsonFile.exists() ){
					//check if xxx_install_status.json exists
					if( !installJsonStatusFile.exists() ){
            			if( logger.isDebugEnabled() ){
                    		logger.debug( "--Before thread submitting status install json file stil does not exist--" );
                    	}						
		        		threadsPool.submit( new Runnable(){
		            		@Override
		        			public void run(){
		            			if( logger.isDebugEnabled() ){
		                    		logger.debug( "--Start running within by thread, serverNodeId:" + serverNodeId );
		                    	}
								//TODO uncomment following code
								ScriptFilesUtilities.waitForFinishInstallationAndMoveFolderToExecuted( serverNodeId );
								
		                    	if( logger.isDebugEnabled() ){
		                    		logger.debug( "--After waitForFinishInstallationAndMoveFolderToExecuted completed" );
		                    	}
		            		}
		            	} );
					}					
				}				
			}
		}
	}*/
/*
	private static void restoreServerNodesAfterBootstrapAndInstallNotStarted() {

		//TODO here in java code there is nothing to do, 
		//since nodejs will move these scripts and will run bootstrap/install
	}*/

	private static void restoreBootstrappingServerNodes() {

    	List<ServerNode> serverNodesBootstrapping = ServerNode.findByCriteria( 
    			new ServerNode.QueryConf().setMaxRows( -1 ).
    				criteria().
    				setRemote( true ).
    				setWidgetIsNull( false ).
    				setWidgetInstanceIsNull( true ).
    				done() );
    	
    	if( logger.isDebugEnabled() ){
    		logger.debug( "---restoreBootstrappingServerNodes, " +
    				"serverNodesBootstrapping size=" + serverNodesBootstrapping.size() );
    		logger.debug( "---restoreBootstrappingServerNodes=" + 
    				Arrays.toString( serverNodesBootstrapping.toArray( 
    						new ServerNode[ serverNodesBootstrapping.size() ] ) ) );
    		
    		List<Widget> allWidgets = Widget.find.all();
    		logger.debug( "---All widgets=" + 
    			Arrays.toString( allWidgets.toArray( new ServerNode[ allWidgets.size() ] ) ) );
    		List<WidgetInstance> allWidgetInstances = WidgetInstance.find.all();
    		logger.debug( "---allWidgetInstances=" + 
    			Arrays.toString( allWidgetInstances.toArray( new ServerNode[ allWidgetInstances.size() ] ) ) );
    		
    	}
    	for( final ServerNode serverNode : serverNodesBootstrapping ){
        	if( logger.isDebugEnabled() ){
        		logger.debug( "--restoreBootstrappingServerNodes, Bootstrapping serverNode ID=" + 
        						serverNode.getId() + ", serverNode=" + serverNode );
        	}
        	
        	final String serverNodeId = String.valueOf( serverNode.getId() );
        	boolean bootstrappingFinished = 
        			ScriptFilesUtilities.isBootstrappingFinished( serverNodeId, serverNodeId );
        	if( !bootstrappingFinished ){

            	if( logger.isDebugEnabled() ){
            		logger.debug( "--restoreBootstrappingServerNodes, before submitting server node to threads pool:" + serverNodeId );
            	}
            	
        		//run waiting for bootstrap finishing in different thread
                Akka.system().scheduler().scheduleOnce( 
                	Duration.create(0, TimeUnit.SECONDS), new Runnable(){
            		@Override
        			public void run(){
            			if( logger.isDebugEnabled() ){
                    		logger.debug( "--Start running within by thread ( restore booostrapping ), serverNodeId:" + serverNodeId );
                    	}
            			ScriptFilesUtilities.waitForFinishBootstrappingAndSaveServerNode( serverNode, null );
            			Widget widget = serverNode.getWidget();
                    	if( logger.isDebugEnabled() ){
                    		logger.debug( "--restoreExecutions, Bootstrapping" +
                    						", serverNode ID=" + serverNodeId + 
                    						", widget=" + widget + 
                    						", serverNode=" + serverNode );
                    	}
                    	if( widget != null ){
                    		//install application
                    		ApplicationContext.get().getWidgetServer().deploy( widget, serverNode, null );	
                    	}
            		}
            	} );
        	}
    	}		
	}
/*
	private static void restoreServerNodesBeforeBootstrap() {

		File newScriptsFolder = new File( NEW_SCRIPTS_FOLDER_PATH );
		if( newScriptsFolder.exists() ){
			String[] list = newScriptsFolder.list();
			//TODO here in java code there is nothing to do since nodejs will move these scripts and will run bootstrap/install
		}
	}*/
}