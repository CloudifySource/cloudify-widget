package beans;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/20/13
 * Time: 1:52 PM
 */
public interface Wait {

    public boolean waitUntil( Test resolved );

    public static interface Test{
        public boolean resolved();
    }

}
