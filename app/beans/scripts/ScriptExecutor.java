package beans.scripts;

import java.io.File;

import models.ServerNode;

import org.apache.commons.exec.CommandLine;
import org.jclouds.compute.ComputeServiceContext;

public interface ScriptExecutor {
	
    /**
	 * Used for bootstrapping
	 * @param cmdLine
	 * @param serverNode
	 * @param jCloudsContext
	 * @param cloudFolder
	 * @param cloudBootstrapConfiguration
	 * @param isHandlePrivateKey
	 */
	public void runBootstrapScript( CommandLine cmdLine, ServerNode serverNode, 
							ComputeServiceContext jCloudsContext, File cloudFolder,
							boolean isHandlePrivateKey );	
	/**
	 * used for running install and uninstall of applications
	 * @param cmdLine
	 * @param server
	 */
    public void runInstallationManagementScript( CommandLine cmdLine, ServerNode server );

    public String getOutput( ServerNode serverNode );
    
    public void onLoad();
}