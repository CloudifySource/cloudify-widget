package clouds.softlayer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.jclouds.compute.ComputeServiceContext;

import utils.CloudifyFactory;
import utils.StringUtils;

import beans.config.CloudProvider;
import beans.config.Conf;
import beans.config.ServerConfig.CloudBootstrapConfiguration;


/**
 * Softlayer Cloud utility methods
 * @author evgenyf
 * Date: 10/10/13
 */
public class SoftlayerCloudUtils {
	
	public static final String SOFTLAYER_PROPERTIES_FILE_NAME = CloudProvider.SOFTLAYER.label + "-cloud.properties";
	
	
	public static File createSoftlayerCloudFolder( String userId, String secretKey, Conf conf ) throws IOException {

		String cloudifyBuildFolder = conf.server.environment.cloudifyHome;
		File cloudifyEscFolder = new File(cloudifyBuildFolder, conf.server.cloudBootstrap.cloudifyEscDirRelativePath);		
		File origCloudFolder = getOriginSoftlayerCloudFolder( conf );
		File destFolder = new File(cloudifyEscFolder, CloudProvider.SOFTLAYER.label + CloudifyFactory.getTempSuffix()); 
		FileUtils.copyDirectory(origCloudFolder, destFolder);

        try {
            File propertiesFile = new File(destFolder, SOFTLAYER_PROPERTIES_FILE_NAME );

            // GUY - Important - Note - Even though this is the "properties" files, it is not used for "properties" per say
            // we are actually writing a groovy file that defines variables.
            Collection<String> newLines = new LinkedList<String>();
            newLines.add("");
            newLines.add("user="+ StringUtils.wrapWithQuotes(userId));
            newLines.add("apiKey="+ StringUtils.wrapWithQuotes(secretKey));
            FileUtils.writeLines( propertiesFile, newLines, true );

            return destFolder;
        } catch (Exception e) {
            throw new RuntimeException( String.format("error while writing cloud properties"), e );
        }
	}	
	
	public static File getOriginSoftlayerCloudFolder( Conf conf ){
		CloudBootstrapConfiguration cloudConf = conf.server.cloudBootstrap;
		String cloudifyBuildFolder = conf.server.environment.cloudifyHome;
		File cloudifyEscFolder = new File(cloudifyBuildFolder, cloudConf.cloudifyEscDirRelativePath);

		//copy the content of hp configuration files to a new folder
        return new File( cloudifyEscFolder, CloudProvider.SOFTLAYER.label );		
	}
	
	/*
	public static String getSoftlayerManagementMachinePrefix( Conf conf ){
		
		String retValue = null;
    	File originSoftlayerCloudFolder = CloudifyFactory.getOriginSoftlayerCloudFolder(conf);
    	File propertiesFile = 
    		new File( originSoftlayerCloudFolder, CloudifyFactory.SOFTLAYER_PROPERTIES_FILE_NAME );
    	if( propertiesFile.exists() ){
    		Properties softlayerCloudProperties = new Properties();
    		try {
    			FileInputStream in = new FileInputStream( propertiesFile );
				softlayerCloudProperties.load( in );
			} 
    		catch( IOException e ) {
    			logger.error( e.toString(), e );
			}
    		String managerMachinePrefix =
    				softlayerCloudProperties.getProperty( CloudifyFactory.SOFTLAYER_MANAGER_MACHINE_PREFIX );
    		
    		managerMachinePrefix = StringUtils.removeStart(managerMachinePrefix, "\"");
    		managerMachinePrefix = StringUtils.removeEnd(managerMachinePrefix, "\"");
   			retValue = managerMachinePrefix;
    	}
    	if( utils.StringUtils.isEmptyOrSpaces( retValue ) ){
    		retValue = conf.server.cloudBootstrap.existingManagementMachinePrefix;
    	}		
    	
    	return retValue;
	}*/
	
	public static void createCloudifySecurityGroup( ComputeServiceContext context, CloudBootstrapConfiguration cloudConf ) {
		//not implemented
	}
	
	
}
