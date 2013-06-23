package beans;

import beans.config.CloudProvider;
import beans.config.Conf;

import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/13/13
 * Time: 9:41 AM
 */
public class NovaCloudCredentials implements  CloudCredentials {
    public String project;
    public String key;
    public String secretKey;
    public CloudProvider cloudProvider;
    public boolean apiCredentials;
    public String zone;

    @Inject
    public Conf conf;

    public NovaCloudCredentials() {

    }

    public NovaCloudCredentials setProject(String project) {
        this.project = project;
        return this;
    }

    // defaults until we have better clouds support.
    public void init(){
        this.zone = conf.server.cloudBootstrap.zoneName; //default. this should be given in the API call evetually.
        apiCredentials = true;
        cloudProvider = CloudProvider.HP;
    }

    public NovaCloudCredentials setKey(String key) {
        this.key = key;
        return this;
    }

    public NovaCloudCredentials setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public String getProject() {
        return project;
    }

    public String getKey() {
        return key;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public NovaCloudCredentials setApiCredentials(boolean apiCredentials) {
        this.apiCredentials = apiCredentials;
        return this;
    }

    public NovaCloudCredentials setZone(String zone) {
        this.zone = zone;
        return this;
    }

    public CloudProvider getCloudProvider() {
        return cloudProvider;
    }

    public NovaCloudCredentials setCloudProvider(CloudProvider cloudProvider) {
        this.cloudProvider = cloudProvider;
        return this;
    }

    @Override
    public String getIdentity() {
        return project + ":" + key;
    }

    @Override
    public String getCredential() {
        return secretKey;
    }

    public void setConf(Conf conf) {
        this.conf = conf;
    }

    @Override
    public String toString() {
        return "NovaCloudCredentials{" +
                "project='" + project + '\'' +
                ", key='" + key + '\'' +
                ", secretKey='***'" +
                ", cloudProvider=" + cloudProvider +
                ", apiCredentials=" + apiCredentials +
                ", zone='" + zone + '\'' +
                ", conf=" + conf +
                '}';
    }
}
