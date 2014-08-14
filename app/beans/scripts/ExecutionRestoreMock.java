package beans.scripts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/11/14
 * Time: 8:20 PM
 */
public class ExecutionRestoreMock implements IExecutionRestore {
    private static Logger logger = LoggerFactory.getLogger(ExecutionRestoreMock.class);

    @Override
    public void restoreExecutions() {
        logger.info("restoring executions mock!!!!");
    }
}
