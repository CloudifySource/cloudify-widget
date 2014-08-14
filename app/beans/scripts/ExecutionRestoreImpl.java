package beans.scripts;

import akka.util.Duration;
import cloudify.widget.common.asyncscriptexecutor.IAsyncExecution;
import models.ServerNode;
import models.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Akka;
import server.ApplicationContext;
import utils.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/11/14
 * Time: 8:21 PM
 */
public class ExecutionRestoreImpl implements IExecutionRestore {


    private static Logger logger = LoggerFactory.getLogger(ExecutionRestoreImpl.class);

    private ScriptExecutor scriptExecutor;
    public void restoreExecutions() {
        restoreBootstrappingServerNodes();
    }


    private void restoreBootstrappingServerNodes() {

        List<ServerNode> serverNodesBootstrapping = ServerNode.findByCriteria(
                new ServerNode.QueryConf().
                        criteria().
                        setAsyncBootstrapStartIsNull(false).
                        setAsyncInstallStartIsNull(true).
                        done());

        logger.info("found [{}] servers to restore", CollectionUtils.size(serverNodesBootstrapping));

        if (CollectionUtils.isEmpty(serverNodesBootstrapping)) {
            logger.info("nothing to restore");
            return;
        }


        for (final ServerNode serverNode : serverNodesBootstrapping) {
            restoreServerNode(serverNode);
        }
    }

    private void restoreServerNode(final ServerNode serverNode) {
        logger.info("restoring execution on serverNode ID=[{}]", serverNode, serverNode.getId());

        IAsyncExecution bootstrapExecution = scriptExecutor.getBootstrapExecution(serverNode);

        if (!bootstrapExecution.isFinished()) {
            logger.info("execution did not finish on nodeId [{}]", serverNode.getId());

            //run waiting for bootstrap finishing in different thread
            Akka.system().scheduler().scheduleOnce(
                    Duration.create(0, TimeUnit.SECONDS), new ResumeExecutionTask(serverNode, bootstrapExecution ));
        }
    }

    public class ResumeExecutionTask implements Runnable {

        private ServerNode serverNode;
        private IAsyncExecution execution;

        public ResumeExecutionTask(ServerNode serverNode, IAsyncExecution execution ) {

            this.serverNode = serverNode;
            this.execution = execution;
        }

        @Override
        public void run() {
            logger.info("waiting for bootstrap on [{}]", serverNode.getId());
            ScriptFilesUtilities.waitForFinishBootstrappingAndSaveServerNode(serverNode, execution);
            Widget widget = serverNode.getWidget();
            logger.info("bootstrap finished on [{}], installing widget [{}] ", serverNode.getId(), widget.getProductName());
            if (widget != null) {
                //install application
                ApplicationContext.get().getWidgetServer().deploy(widget, serverNode, null);
            } else {
                logger.info("widget is null on [{}]. deleting serverNode", serverNode.getId());
                serverNode.delete();
            }
        }
    }

    public ScriptExecutor getScriptExecutor() {
        return scriptExecutor;
    }

    public void setScriptExecutor(ScriptExecutor scriptExecutor) {
        this.scriptExecutor = scriptExecutor;
    }
}
