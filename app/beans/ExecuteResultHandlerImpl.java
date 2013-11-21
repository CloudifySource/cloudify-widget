package beans;

import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 11/6/13
 * Time: 10:02 PM
 */
public class ExecuteResultHandlerImpl extends DefaultExecuteResultHandler {
    private static Logger logger = LoggerFactory.getLogger(ExecuteResultHandlerImpl.class);

    private String name;
    private boolean finished = false;
    @Override
    public void onProcessComplete(int i) {
        logger.info("process [{}] has finished with exit code [{}]", name, i);
        finished = true;
        super.onProcessComplete( i );
    }

    @Override
    public void onProcessFailed(ExecuteException e) {
        logger.error("process [{}] failed due to error", name, e);
        finished = true;
        super.onProcessFailed( e );
    }

    public ExecuteResultHandlerImpl setName(String name) {
        this.name = name;
        return this;
    }
}
