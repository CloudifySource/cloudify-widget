package server.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/7/14
 * Time: 11:14 PM
 */
public class Response401 extends RuntimeException {
    public Response401() {
        super();
    }

    public Response401(String message) {
        super(message);
    }

    public Response401(String message, Throwable cause) {
        super(message, cause);
    }

    public Response401(Throwable cause) {
        super(cause);
    }
}
