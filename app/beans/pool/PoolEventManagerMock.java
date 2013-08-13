package beans.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/13/13
 * Time: 12:37 PM
 */
public class PoolEventManagerMock implements PoolEventListener{
    private static Logger logger = LoggerFactory.getLogger(PoolEventManagerMock.class);
    @Override
    public void handleEvent(PoolEvent poolEvent) {
        logger.info("handling pool event [{}]", poolEvent);
    }
}
