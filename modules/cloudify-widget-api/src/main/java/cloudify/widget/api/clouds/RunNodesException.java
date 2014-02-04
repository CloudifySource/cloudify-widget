package cloudify.widget.api.clouds;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/4/14
 * Time: 12:32 PM
 */
public class RunNodesException extends Exception{
    public RunNodesException() {
        super();
    }

    public RunNodesException(String message) {
        super(message);
    }

    public RunNodesException(String message, Throwable cause) {
        super(message, cause);
    }

    public RunNodesException(Throwable cause) {
        super(cause);
    }
}
