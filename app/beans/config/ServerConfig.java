/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package beans.config;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cloudify.widget.api.clouds.CloudProvider;
import cloudify.widget.api.clouds.MachineOptions;
import cloudify.widget.softlayer.SoftlayerConnectDetails;
import cloudify.widget.softlayer.SoftlayerMachineOptions;
import org.apache.commons.lang3.StringUtils;
import org.reflections.ReflectionUtils;

import utils.Utils;

import com.google.common.base.Predicate;

/**
 * User: guym
 * Date: 12/13/12
 * Time: 3:12 PM
 */
public class ServerConfig {


    public static enum DestroyMethod{
        SCHEDULE, INTERVAL
    }

    public DestroyMethod destroyMethod = DestroyMethod.SCHEDULE;

    public long destroyServerIntervalMillis = Utils.parseTimeToMillis("1mn");

    public PoolConfiguration pool = new PoolConfiguration();


    public ScriptEnvironmentConf environment = new ScriptEnvironmentConf();


    public BootstrapConfiguration bootstrap = new BootstrapConfiguration();
    
    public CloudBootstrapConfiguration cloudBootstrap = new CloudBootstrapConfiguration();

    public DefaultAdmin admin = new DefaultAdmin();
    
    public CloudProvider cloudProvider = CloudProvider.HP;

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
        public long maxExpirationTimeMillis = Utils.parseTimeToMillis("60mn");
        @Config( ignoreNullValues = true )
        public long minExpiryTimeMillis = Utils.parseTimeToMillis("20mn");

    }

    /**
     *
     *
     * this is a configuration for system environment variables while running scripts.
     * it will help us remove hard-coded strings like cloudify home location and enable us to have
     * more flexible environment in development and production.
     *
     *
     * We need a Java Object for holding the environment variables for 2 reasons
     * 1. We already have a good support for configuration and it would be a shame not to use it
     *      For example - this way we get a print of all the variables.
     * 2. We might need the value of the variable in Java code in a non script related matter.
     * 3. We can validate the values
     *
     *
     *
     * NOTE : I assume that all properties are STRINGs at the moment..
     *
     **/


    public static class ScriptEnvironmentConf {

        @Environment( key = "CLOUDIFY_HOME" )
        public String cloudifyHome = Utils.getFileByRelativePath("cloudify-folder").getAbsolutePath();

        public boolean useSystemEnvAsDefault = false; // this will also pass JAVA_OPTS, be careful with this! for windows development mainly.

        private Map<String,String> environment = null ;

        public Map<String,String> getEnvironment() {

            try {
                if (environment == null) {
                    environment = new HashMap<String, String>();

                    if ( useSystemEnvAsDefault ){
                        environment.putAll( System.getenv() );
                    }

                    Set<Field> allFields = ReflectionUtils.getAllFields(this.getClass(), new Predicate<Field>() {
                        @Override
                        public boolean apply(Field field) {
                            return field.getType() == String.class && Modifier.isPublic( field.getModifiers() );
                        }
                    });
                    for (Field field : allFields) {
                        String name = field.getName();
                        if (field.isAnnotationPresent(Environment.class)) {
                            Environment envAnnotation = field.getAnnotation(Environment.class);
                            name = StringUtils.isEmpty(envAnnotation.key()) ? name : envAnnotation.key();
                        }
                        String value = (String) field.get(this);
                        environment.put(name, value);
                    }
                }
                return environment;
            } catch (Exception e) {
                throw new RuntimeException("unable to populate execution map", e);
            }
        }

        public String getCloudifyHome() {
            return cloudifyHome;
        }
    }

    public static class BootstrapConfiguration{

        public SoftlayerBootstrapConfiguration softlayer = new SoftlayerBootstrapConfiguration();


        // common configuration
        public File script = Utils.getFileByRelativePath("/bin/bootstrap_machine.sh"); // script to run on bootstrap
        public File teardownScript = Utils.getFileByRelativePath("/bin/teardown_machine.sh"); // script to run teardown
        @Config(ignoreNullValues=true)
        public boolean runTeardown = true;
        public String recipeUrl = "";
        public String cloudifyUrl;
        public String urlSecretKey = "";
        public String urlAccessKey = "";
        public String urlEndpoint = "s3.amazonaws.com";
        public String recipeDownloadMethod = "wget";
        public String installNode = "false";
        public String recipeRelativePath;
        public File prebootstrapScript = Utils.getFileByRelativePath("/conf/cloudify/prebootstrap");

        public String tag = null; // the tag to use
        @Config(ignoreNullValues = true)
        public long sleepBeforeBootstrapMillis = Utils.parseTimeToMillis("20s"); // sleep before bootstrap
        @Config(ignoreNullValues = true)
        public int createServerRetries = 3; // retries to create server
        @Config(ignoreNullValues = true)
        public int bootstrapRetries = 3; // retries to bootstrap
        public String bootstrapApplicationUrl = null;


        public SoftlayerBootstrapConfiguration getSoftlayer(){
            return softlayer;
        }
    }

    public static class SoftlayerBootstrapConfiguration{
       public SoftlayerConnectDetails connectDetails = new SoftlayerConnectDetails();
        public SoftlayerMachineOptions machineOptions = new SoftlayerMachineOptions().setLocationId("37473");

        public SoftlayerConnectDetails getConnectDetails() {
            return connectDetails;
        }
        public SoftlayerMachineOptions getMachineOptions() { return machineOptions; }


    }

    public static class ApiCredentials{
        public String project;
        public String key;
        public String secretKey;
        @Config(ignoreNullValues = true)
        public boolean apiAccessKeyCredentials = false; // are this the tenant's details, or the user details? HP makes a difference.
    }
    
    // cloud bootstrap configuration.
    public static class CloudBootstrapConfiguration {
    	public String cloudName = "hp";
        @Config(ignoreNullValues = true)
        public File remoteBootstrap = Utils.getFileByRelativePath("/bin/remote_bootstrap.sh");
        public String keyPairName = "cloudify";
        public String cloudifyHpUploadDirName = "upload";
        public String cloudPropertiesFileName = "hp-cloud.properties";
        public String zoneName = "az-2.region-a.geo-1";
        public String hardwareId = "102";
        public String linuxImageId = "221";
        public String securityGroup = "cloudifySecurityGroup";
        public String cloudifyEscDirRelativePath = "clouds";
        public String existingManagementMachinePrefix = "cloudify-manager";
        @Config( ignoreNullValues =  true )
        public boolean removeCloudFolder; // used in dev environment to see the configuration in case it got corrupted.

        public String hardwareId(){
            return zoneName + "/" + hardwareId;
        }

        public String linuxImageId() {
            return zoneName + "/" + linuxImageId;
        }
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


    public BootstrapConfiguration getBootstrap(){
        return bootstrap;
    }

    public ScriptEnvironmentConf getEnvironment() { return environment; }
}
