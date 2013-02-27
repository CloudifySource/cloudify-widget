package beans;

/**
 * User: guym
 * Date: 2/27/13
 * Time: 9:41 AM
 */
public class ServerNodesPoolStats {
    public int all = -1;
    public int nonRemote = -1;
    public int busyServers = -1;
    public int nonBusyServers = -1;
    public int minLimit = -1;
    public int maxLimit = -1;

    @Override
    public String toString()
    {
        return "ServerNodesPoolStats{" +
                "all=" + all +
                ", nonRemote=" + nonRemote +
                ", busyServers=" + busyServers +
                ", nonBusyServers=" + nonBusyServers +
                ", minLimit=" + minLimit +
                ", maxLimit=" + maxLimit +
                '}';
    }
}
