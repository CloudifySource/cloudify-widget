package beans.cloudbootstrap;

import cloudify.widget.cli.softlayer.AwsEc2CloudBootstrapDetails;
import cloudify.widget.ec2.Ec2AccountIdReader;
import cloudify.widget.ec2.Ec2ImageShare;
import cloudify.widget.ec2.Ec2KeyPairGenerator;
import models.AwsImageShare;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/10/14
 * Time: 7:57 PM
 */
public class AwsEc2CloudProviderCreator extends ACloudProviderCreator {

    private static Logger logger = LoggerFactory.getLogger(AwsEc2CloudProviderCreator.class);

    @Override
    protected void prepareCloudAccount() {
        if ( serverNode.getWidget() == null || serverNode.getWidget().getAwsImageShare() == null ){
            logger.info("no image details to share");
            return;
        }

        if (bootstrapDetails instanceof AwsEc2CloudBootstrapDetails) {
            AwsEc2CloudBootstrapDetails ec2CloudBootstrapDetails = (AwsEc2CloudBootstrapDetails) bootstrapDetails;
            try {

                logger.info("getting account id to share image");
                String accountId = new Ec2AccountIdReader().getAccountId(ec2CloudBootstrapDetails.getKey(), ec2CloudBootstrapDetails.getSecretKey());

                logger.info("sharing image");
                shareImage(serverNode.getWidget().getAwsImageShare(), accountId, Ec2ImageShare.Operation.ADD);
            } catch (Exception e) {
                throw new RuntimeException("unable to share image", e);
            }
        } else {
            throw new RuntimeException("expected AwsEc2CloudBootstrapDetails for " + bootstrapDetails.getClass());
        }
    }

    public void shareImage(AwsImageShare awsImageShare, String accountId, Ec2ImageShare.Operation operation ) {
        Ec2ImageShare imageShare = new Ec2ImageShare();

        imageShare.setPermissions(awsImageShare.getApiKey(),
                awsImageShare.getApiSecretKey(),
                awsImageShare.getEndpoint(),
                awsImageShare.getImageId(),
                operation,
                accountId);
    }

    @Override
    protected void createPrivateKey() {

        Ec2KeyPairGenerator ec2KeyPairGenerator = new Ec2KeyPairGenerator();

        if ( bootstrapDetails instanceof AwsEc2CloudBootstrapDetails  ){

            try {

                AwsEc2CloudBootstrapDetails ec2CloudBootstrapDetails = (AwsEc2CloudBootstrapDetails) bootstrapDetails;
                Ec2KeyPairGenerator.Data data = ec2KeyPairGenerator.generate(ec2CloudBootstrapDetails.getKey(), ec2CloudBootstrapDetails.getSecretKey());
                ec2CloudBootstrapDetails.setKeyPair(data.getName());

                serverNode.setPrivateKey(data.getContent());
                serverNode.save();

                FileUtils.writeStringToFile(new File(bootstrapDetails.getCloudDirectory(), "upload/my.pem"), data.getContent());

                ec2CloudBootstrapDetails.setKeyFile("my.pem");
            }catch(Exception e){
                throw new RuntimeException("unable to create & save private key",e);
            }

        }else{
            throw new RuntimeException("expected AwsEc2CloudBootstrapDetails for " + bootstrapDetails.getClass());
        }
    }
}
