package beans.config;


/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/11/13
 * Time: 2:17 PM
 */
public enum CloudProvider {
    HP("hpcloud-compute"),
    AWS_EC2("aws-ec2"),
    SOFTLAYER("softlayer"),
    NA("na");

    public String label;

    private CloudProvider( String label ) {
        this.label = label;
    }

    public static CloudProvider findByLabel(String cloudProvider) {
        for (CloudProvider provider : values()) {
            if ( provider.label.equals(cloudProvider)){
                return provider;
            }
        }
        throw new IllegalArgumentException(String.format("cannot find cloudProvider with label [%s]", cloudProvider));
    }
}