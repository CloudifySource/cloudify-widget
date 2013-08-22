package beans.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/22/13
 * Time: 3:06 PM
 */
public class DestroyServersTaskMock implements DestroyServersTask {
    private static Logger logger = LoggerFactory.getLogger(DestroyServersTaskMock.class);
    @Override
    public void run() {
        logger.debug("running DestroyServerTask");
    }
}
