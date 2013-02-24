package beans.api;

import org.apache.commons.exec.ExecuteStreamHandler;
import server.WriteEventListener;

/**
 * User: guym
 * Date: 1/29/13
 * Time: 2:52 PM
 */
public interface ProcessStreamHandler extends ExecuteStreamHandler {

    public void setWriteEventListener(WriteEventListener wel);
}
