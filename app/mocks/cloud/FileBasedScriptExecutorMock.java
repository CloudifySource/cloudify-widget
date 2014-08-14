package mocks.cloud;

import beans.config.Conf;
import beans.scripts.ScriptExecutor;
import cloudify.widget.common.asyncscriptexecutor.*;
import models.ServerNode;
import org.apache.commons.exec.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/13/14
 * Time: 1:00 PM
 */
public class FileBasedScriptExecutorMock implements ScriptExecutor{

    private static Logger logger = LoggerFactory.getLogger(FileBasedScriptExecutorMock.class);

    public String nodeId;

    private Conf conf;

    @Override
    public IAsyncExecution runBootstrapScript(CommandLine cmdLine, ServerNode serverNode) {

        IAsyncExecution impl = new AsyncExecutionImpl();
        IAsyncExecutionDetails executionDetails = getExecutionDetails(serverNode, "bootstrap");
        impl.setDetails(executionDetails);
        logger.info("new bootstrap task written to [{}]", executionDetails.getTaskFile());
        return impl;
    }

    public IAsyncExecutionDetails getExecutionDetails( ServerNode serverNode, String action ){
        IAsyncExecutionDetails details = new AsyncExecutionDetails();

        details.setNewScriptsDir( conf.asyncExecution.newScriptsDir);
        details.setTaskFile( new File(conf.asyncExecution.newScriptsDir, String.format("%s_%s.json", nodeId, action)));
        details.setOutputFile( new File(conf.asyncExecution.executingScriptsDir, String.format("%s/output.log", nodeId)));
        details.setStatusFile( new File(conf.asyncExecution.executingScriptsDir, String.format("%s/%s.status", nodeId,action)));

        return details;
    }

    @Override
    public IAsyncExecution getBootstrapExecution(ServerNode serverNode) {
        return null;
    }

    @Override
    public void runInstallationManagementScript(CommandLine cmdLine, ServerNode server) {

    }

    @Override
    public void runTearDownCommand(CommandLine cmdLine) {

    }

    public Conf getConf() {
        return conf;
    }

    public void setConf(Conf conf) {
        this.conf = conf;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
