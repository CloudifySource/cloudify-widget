package beans.cloudbootstrap;

import cloudify.widget.api.clouds.CloudProvider;
import server.ApplicationContext;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/11/14
 * Time: 11:38 AM
 */
public class CloudProviderCreatorFactory {

    public ICloudProviderCreator getCreator( CloudProvider cloudProvider ){
        ICloudProviderCreator creator = null;
        switch(cloudProvider){
            case AWS_EC2: {
                creator = new AwsEc2CloudProviderCreator();
                break;

            }
            case SOFTLAYER: {
                creator = new SoftlayerCloudProviderCreator();
                break;
            }
            default:{
                throw new UnsupportedOperationException("cloud provider is not supported [" + cloudProvider + "]");
            }
        }

        return creator;
    }
}
