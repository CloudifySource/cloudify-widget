package beans.config;

/**
 * User: guym
 * Date: 12/13/12
 * Time: 1:33 PM
 */
public class SmtpConf {
    public String host = "N/A";
    @Config( ignoreNullValues = true )
    public int port = 0;
    @Config( ignoreNullValues = true )
    public boolean tls = false;
    @Config( ignoreNullValues = true )
    public boolean mock = false;
    @Config( ignoreNullValues = true )
    public boolean enabled = true;
    @Config( ignoreNullValues = true )
    public boolean debug = false;
    public String user = "N/A";
    public String password = "N/A";
    @Config( ignoreNullValues = true )
    public boolean ssl = false;
    @Config( ignoreNullValues = true )
    public boolean auth = false;
}
