package beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/20/13
 * Time: 1:52 PM
 */
public class ActiveWait implements Wait {

    private long intervalMillis;

    private long timeoutMillis;

    private long startTimeMillis;

    private static Logger logger = LoggerFactory.getLogger(ActiveWait.class);

    @Override
    public boolean waitUntil(Wait.Test resolved) {
        startTimeMillis = System.currentTimeMillis();
//        logger.info("waiting for serverId activation [{}]", serverId);
        long startTime = System.currentTimeMillis();
        logger.info("waiting for test [{}] while sleeping intervals of [{}]ms with timeout of [{}]ms", new Object[]{resolved.toString(), intervalMillis, timeoutMillis});
        for ( long time = System.currentTimeMillis(); time - startTime < timeoutMillis ; Utils.threadSleep(intervalMillis), time = System.currentTimeMillis() )
        {
//            logger.info("Waiting for a server activation... Left timeout: {} sec", startTime + timeoutSeconds - time);
//            if (serverApi.get(serverId).getStatus().equals(status)){
//                return true; // active
                if ( resolved.resolved() ){
                    return true;
                }
        }
        return false;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }

    public long getTimeLeftMillis(){
        return startTimeMillis + timeoutMillis - System.currentTimeMillis();
    }

    public ActiveWait setIntervalMillis(long intervalMillis) {
        this.intervalMillis = intervalMillis;
        return this;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public ActiveWait setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }
}
