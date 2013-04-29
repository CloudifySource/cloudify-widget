package server.exceptions;

/**
 * User: eliranm
 * Date: 4/25/13
 * Time: 5:06 PM
 */
public class BootstrapException extends Exception {
    public BootstrapException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public BootstrapException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public BootstrapException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public BootstrapException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
