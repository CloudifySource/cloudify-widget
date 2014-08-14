package beans.scripts;

import java.io.File;

import cloudify.widget.common.asyncscriptexecutor.IAsyncExecution;
import models.ServerNode;

import org.apache.commons.exec.CommandLine;
import org.jclouds.compute.ComputeServiceContext;

public interface ScriptExecutor{
	
    /**
	 * Used for bootstrapping
	 * @param cmdLine
	 * @param serverNode
	 */
	public IAsyncExecution runBootstrapScript( CommandLine cmdLine, ServerNode serverNode );

    /**
     *
     * @param serverNode - the node we are running the execution on.
     * @return a populated async execution without writing/executing it.
     *
     * good when you want to be able to query if execution finished after you lost a reference to it.
     */
    public IAsyncExecution getBootstrapExecution( ServerNode serverNode );
	/**
	 * used for running install and uninstall of applications
	 * @param cmdLine
	 * @param server
	 */
    public void runInstallationManagementScript( CommandLine cmdLine, ServerNode server );

    public void runTearDownCommand( CommandLine cmdLine );

}