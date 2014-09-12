package beans.cloudbootstrap;

import beans.CustomPropertiesWriter;
import cloudify.widget.cli.ICloudBootstrapDetails;
import cloudify.widget.common.StringUtils;
import cloudify.widget.common.WidgetResourcesUtils;
import models.ServerNode;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import server.ApplicationContext;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/10/14
 * Time: 7:27 PM
 */
public class SoftlayerCloudProviderCreator extends ACloudProviderCreator{

    private static Logger logger = LoggerFactory.getLogger(SoftlayerCloudProviderCreator.class);

    @Override
    protected void prepareCloudAccount() {
        logger.info("not need to prepare account");
    }

    @Override
    protected void createPrivateKey() {
        logger.info("no need for private key");
    }
}
