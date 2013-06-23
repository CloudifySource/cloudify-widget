package beans.pool;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/19/13
 * Time: 11:29 PM
 */
public interface PoolEventListener {
    void handleEvent(PoolEvent poolEvent);
}
