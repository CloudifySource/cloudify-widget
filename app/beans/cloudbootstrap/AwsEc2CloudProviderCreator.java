package beans.cloudbootstrap;

import cloudify.widget.allclouds.executiondata.ExecutionDataModel;
import cloudify.widget.cli.softlayer.AwsEc2CloudBootstrapDetails;
import cloudify.widget.ec2.Ec2AccountIdReader;
import cloudify.widget.ec2.Ec2ImageShare;
import cloudify.widget.ec2.Ec2KeyPairGenerator;
import cloudify.widget.ec2.executiondata.AwsEc2ExecutionModel;
import models.AwsImageShare;
import models.ServerNode;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.StringUtils;

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
        AwsImageShare imageShareDetails = getImageShareDetails();
        if ( imageShareDetails == null || !imageShareDetails.isValid()){
            logger.info("no image details to share, or not enough details to share");
            return;
        }

        if (bootstrapDetails instanceof AwsEc2CloudBootstrapDetails) {
            AwsEc2CloudBootstrapDetails ec2CloudBootstrapDetails = (AwsEc2CloudBootstrapDetails) bootstrapDetails;
            try {

                logger.info("getting account id to share image");
                String accountId = new Ec2AccountIdReader().getAccountId(ec2CloudBootstrapDetails.getKey(), ec2CloudBootstrapDetails.getSecretKey());

                logger.info("sharing image");
                shareImage(accountId);

            } catch (Exception e) {
                throw new RuntimeException("unable to share image", e);
            }
        } else {
            throw new RuntimeException("expected AwsEc2CloudBootstrapDetails for " + bootstrapDetails.getClass());
        }
    }


    /**
     *
     * Users define awsImageShare data by defining it on the widget.
     * You can override the imageId and endpoint by front-end API.
     * so we need to merge all the information we have here.
     *
     * @return the merged data between what was defined in the widget and what was given by frontend API.
     */
    private AwsImageShare getImageShareDetails(){
        if ( serverNode.getWidget() == null ){
            return null;
        }

        if ( serverNode.getWidget().getAwsImageShare() == null ){
            return null;
        }

        AwsImageShare awsImageShare = null;
        try {
            awsImageShare = (AwsImageShare) BeanUtils.cloneBean(serverNode.getWidget().getAwsImageShare());
        }catch(Exception e){
            throw new RuntimeException("unable to clone aws image share data");
        }

        if ( awsImageShare == null ){
            return null;
        }

        // try overriding with execution model
        try {
            ExecutionDataModel executionDataModel = serverNode.getExecutionDataModel();
            if (executionDataModel != null ){
                AwsEc2ExecutionModel awsEc2ExecutionModel = executionDataModel.getAwsEc2ExecutionModel();
                if ( awsEc2ExecutionModel != null ) {
                    if ( !StringUtils.isEmptyOrSpaces(awsEc2ExecutionModel.endpoint) ){
                        awsImageShare.setEndpoint( awsEc2ExecutionModel.endpoint);
                    }

                    if ( !StringUtils.isEmptyOrSpaces(awsEc2ExecutionModel.imageId)){
                        awsImageShare.setImageId( awsEc2ExecutionModel.imageId);
                    }
                }
            }
        }catch(Exception e){
            throw new RuntimeException("unable to override with execution model",e);
        }

        return awsImageShare;
    }

    /**
     * Gets the default image sharing
     * @param accountId
     */
    private void shareImage( String accountId ){
        shareImage(serverNode.getWidget().getAwsImageShare(), accountId, Ec2ImageShare.Operation.ADD);
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
