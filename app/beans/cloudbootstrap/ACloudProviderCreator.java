package beans.cloudbootstrap;

import beans.CustomPropertiesWriter;
import cloudify.widget.cli.ICloudBootstrapDetails;
import cloudify.widget.cli.ICloudifyCliHandler;
import cloudify.widget.common.StringUtils;
import cloudify.widget.common.WidgetResourcesUtils;
import models.ServerNode;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import server.ApplicationContext;
import utils.ResourceManagerFactory;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/10/14
 * Time: 7:19 PM
 *
 * This class will copy the folder of the cloud provider.
 *
 */
public abstract class ACloudProviderCreator implements ICloudProviderCreator
{

    protected ServerNode serverNode;

    protected ICloudifyCliHandler cliHandler = ApplicationContext.get().getCloudifyCliHandler();

    protected ICloudBootstrapDetails bootstrapDetails;

    private static Logger logger = LoggerFactory.getLogger(ACloudProviderCreator.class);

    private ResourceManagerFactory resourceManagerFactory;


    /**
     *
     *
     * Copies the cloud directory for this server node's execution.
     *
     * Writes the properties files (advanced + custom) to the cloud's properties file.
     *
     * Supports 2 types of cloud providers:
     *
     *  - the ones that come built in with cloudify
     *  - external providers that are available by a URL as a ZIP file.
     *
     *
     * @param serverNode - the serverNode we want to create a new folder.
     * @return the File indicating location of new cloud folder.
     */
    public File createCloudProvider( ServerNode serverNode ){

        logger.info("creating cloud provider for [{}]", serverNode.toDebugString());
        try {
            init(serverNode);
            prepareCloudAccount();
            generateFolder();
            createPrivateKey();
            writeProperties();
            return getCloudProviderFolder();
        }catch(UnsupportedOperationException e){
            logger.info("unsupported cloud provider",e);
            serverNode.errorEvent(e.getMessage()).save();
            throw e;
        } catch (Exception e ){
            logger.error("failed creating cloud provider",e);

            serverNode.errorEvent("Missing cloud provider details").save();
            throw new RuntimeException(e);
        }
    }


    protected void init( ServerNode serverNode ){
        this.serverNode = serverNode;
        this.bootstrapDetails = serverNode.getExecutionDataModel().getCloudBootstrapDetails( serverNode.getWidget().cloudProvider );
    }

    abstract protected void prepareCloudAccount();

    protected void generateFolder(){
        logger.info("getting bootstrap details");

        logger.info("I got bootstrap details");
        downloadProvider(  );
        useCloudName(  );


    }

    private void useCloudName() {
        try {
            if (!StringUtils.isEmpty(serverNode.getWidget().getCloudName())) {

                String cloudName = serverNode.getWidget().getCloudName();
                // in case we simply have a cloud name, we construct the relevant paths
                File cloudsBaseDir = new File(ApplicationContext.get().conf().server.environment.cloudifyHome, "clouds");
                File myCloudDir = new File(cloudsBaseDir, cloudName);
                File myCloudDirCopy = new File(myCloudDir.getAbsolutePath() + "_" + System.currentTimeMillis());
                FileUtils.copyDirectory(myCloudDir, myCloudDirCopy);
                File propertiesFile = new File(myCloudDirCopy, cloudName + "-cloud.properties");

                bootstrapDetails.setCloudDirectory(myCloudDirCopy.getAbsolutePath());
                bootstrapDetails.setCloudPropertiesFile(propertiesFile);

            }
        } catch (Exception e) {
            logger.error("error while using cloud name", e);
            throw new RuntimeException("error while using cloud name", e);
        }
    }

    private void downloadProvider( ){
        if ( serverNode.getWidget().hasCloudProviderData() ){
            logger.info("server node has cloud provider data");
            try {

                CloudProviderDetails provider = Json.fromJson(serverNode.getWidget().getCloudProvideJson(), CloudProviderDetails.class);
                WidgetResourcesUtils.ResourceManager cloudProviderManager = resourceManagerFactory.getCloudProviderManager( serverNode.getWidget() );
                String baseDir = ApplicationContext.get().conf().resources.cloudProvidersBaseDir.getAbsolutePath();
                File myCloudDirCopy = new File(baseDir, "server_node_" + serverNode.getId() );
                if ( serverNode.getWidget().isAutoRefreshProvider() ){
                    cloudProviderManager.copyFresh( myCloudDirCopy );
                }else{
                    cloudProviderManager.copyFromCache(myCloudDirCopy);
                }

                if ( !StringUtils.isEmptyOrSpaces(serverNode.getWidget().cloudProviderRootDir) ){
                    myCloudDirCopy = new File(myCloudDirCopy,serverNode.getWidget().cloudProviderRootDir );
                }

                logger.info("using folder [{}] as cloud provider", myCloudDirCopy);


                File myCloudRoot = myCloudDirCopy;
                if ( !StringUtils.isEmptyOrSpaces(provider.rootPath) ){
                    myCloudRoot = new File(myCloudRoot, provider.rootPath );
                }


                bootstrapDetails.setCloudDirectory( myCloudRoot.getAbsolutePath() );
                bootstrapDetails.setCloudPropertiesFile( new File(myCloudRoot, provider.propertiesFileName ));

            }catch(Exception e){
                logger.error("unable to parse cloud provider json [{}]" , serverNode.getWidget().getData());
            }
        }
    }

    protected void writeProperties( ){
        cliHandler.writeBootstrapProperties(bootstrapDetails);

        logger.info("cloud bootstrap cloud directory is [{}]", bootstrapDetails.getCloudDirectory() );
        logger.info("cloud bootstrap properties file is [{}]", bootstrapDetails.getCloudPropertiesFile().getAbsolutePath() );

        File bootstrapPropertiesFile = bootstrapDetails.getCloudPropertiesFile();
        new CustomPropertiesWriter().writeProperties(serverNode, bootstrapPropertiesFile);

    }
    abstract protected void createPrivateKey();

    protected File getCloudProviderFolder(){
        return new File(bootstrapDetails.getCloudDirectory());
    }


    public void setCliHandler(ICloudifyCliHandler cliHandler) {
        this.cliHandler = cliHandler;
    }
}
