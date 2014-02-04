package cloudify.widget.api.clouds;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/4/14
 * Time: 1:41 PM
 */
public class ServerIp {
    public String publicIp = null;
    public String privateIp = null;

    @Override
    public String toString() {
        return "ServerIp{" +
                "publicIp='" + publicIp + '\'' +
                ", privateIp='" + privateIp + '\'' +
                '}';
    }
}
