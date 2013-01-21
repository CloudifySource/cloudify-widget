package beans.config;

import utils.Utils;

import java.io.File;

/**
 * User: guym
 * Date: 12/13/12
 * Time: 3:12 PM
 */
public class ServerConfig {

    public PoolConfiguration pool = new PoolConfiguration();

    public BootstrapConfiguration bootstrap = new BootstrapConfiguration();
    
    public CloudBootstrapConfiguration cloudBootstrap = new CloudBootstrapConfiguration();

    public DefaultAdmin admin = new DefaultAdmin();

    @Config( ignoreNullValues = true )
    public long sessionTimeoutMillis = Utils.parseTimeToMillis( "15mn" );

    public static class PoolConfiguration{
        @Config( ignoreNullValues = true )
        public boolean coldInit = false ;
        @Config( ignoreNullValues = true )
        public int minNode = 2;
        @Config( ignoreNullValues = true )
        public int maxNodes = 5;
        @Config( ignoreNullValues = true )
        public long expirationTimeMillis = Utils.parseTimeToMillis("60mn");
        @Config( ignoreNullValues = true )
        public long maxExpirationTimeMillis = Utils.parseTimeToMillis("30mn");
        @Config( ignoreNullValues = true )
        public long minExpiryTimeMillis = Utils.parseTimeToMillis("10mn");

    }

    public static class BootstrapConfiguration{
        public String serverNamePrefix="cloudify_pool_server";
        public String zoneName="az-1.region-a.geo-l";
        public String keyPair="cloudify";
        public String securityGroup="default";
        public String flavorId="102";
        public String imageId="1358";
        public SshConfiguration ssh = new SshConfiguration();
        public String apiKey="<HP cloud Password>";
        public String username="<tenant>:<user>";
        public String cloudProvider="hpcloud-compute";
        public File script;
    }
    
    // cloud bootstrap configuration.
    public static class CloudBootstrapConfiguration {
    	public String cloudName = "hp";
        public File remoteBootstrap = Utils.getFileByRelativePath("/bin/remote_bootstrap");
        public String keyPairName = "cloudify";
        public String cloudifyHpUploadDirName = "upload";
        public String cloudPropertiesFileName = "hp-cloud.properties";
        public String zoneName = "az-2.region-a.geo-1";
        public String hardwareId = zoneName + "/102";
        public String linuxImageId = zoneName + "/221";
        public String securityGroup = "default";
        public String cloudProvider = "hpcloud-compute";
    }

    public static class SshConfiguration{
        public String user="root";
        @Config( ignoreNullValues = true )
        public int port=22;
        public File privateKey= Utils.getFileByRelativePath( "/bin/hpcloud.pem" );
    }

    public static class DefaultAdmin{
        public String username = "admin@cloudifysource.org";
        public String password = "admin1324";
    }
}
