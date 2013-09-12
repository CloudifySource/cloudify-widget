package beans;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/9/13
 * Time: 7:41 PM
 */
public class NoOpCallback implements Runnable{
    public static NoOpCallback instance = new NoOpCallback();
    @Override
    public void run() {

    }
}
