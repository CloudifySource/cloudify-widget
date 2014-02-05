package utils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import cloudify.widget.api.clouds.CloudProvider;
import org.apache.commons.io.FileUtils;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.ApplicationContext;
import beans.config.Conf;
import beans.config.ServerConfig.CloudBootstrapConfiguration;


/**
 * 
 * @author evgenyf
 * Date: 10/7/13
 */
public class CloudifyFactory {
	
	private static Logger logger = LoggerFactory.getLogger(CloudifyUtils.class);	
	
	/**
	 * Creates a cloud folder containing all necessary credentials 
	 * for bootstrapping to the HP cloud.
	 * 
	 * @return
	 * 			A path to the newly created cloud folder.
	 * @throws IOException
	 */
	public static File createCloudFolder(CloudProvider cloudProvider, String project, String key, String secretKey, ComputeServiceContext context) throws IOException {

		CloudBootstrapConfiguration cloudConf = ApplicationContext.get().conf().server.cloudBootstrap;
		String cloudifyBuildFolder = ApplicationContext.get().conf().server.environment.cloudifyHome;
		File cloudifyEscFolder = new File(cloudifyBuildFolder, cloudConf.cloudifyEscDirRelativePath);

		//copy the content of hp configuration files to a new folder
        File origCloudFolder = new File( cloudifyEscFolder, cloudConf.cloudName );
		File destFolder = new File(cloudifyEscFolder, cloudConf.cloudName + getTempSuffix()); 
		FileUtils.copyDirectory(origCloudFolder, destFolder);

		// create new pem file using new credentials.
		File pemFolder = new File(destFolder, cloudConf.cloudifyHpUploadDirName);
		File newPemFile = createPemFile( cloudProvider, context );
		FileUtils.copyFile(newPemFile, new File(pemFolder, newPemFile.getName() +".pem"), true);

        try {
            File propertiesFile = new File(destFolder, cloudConf.cloudPropertiesFileName);

            // GUY - Important - Note - Even though this is the "properties" files, it is not used for "properties" per say
            // we are actually writing a groovy file that defines variables.
            Collection<String> newLines = new LinkedList<String>();
            newLines.add("");
            newLines.add("tenant="+ StringUtils.wrapWithQuotes(project));
            newLines.add("user="+ StringUtils.wrapWithQuotes(key));
            newLines.add("apiKey="+ StringUtils.wrapWithQuotes(secretKey));
            newLines.add("keyFile="+ StringUtils.wrapWithQuotes(newPemFile.getName() + ".pem"));
            newLines.add("keyPair="+ StringUtils.wrapWithQuotes(newPemFile.getName()));
            newLines.add("securityGroup="+ StringUtils.wrapWithQuotes(cloudConf.securityGroup));
            FileUtils.writeLines( propertiesFile, newLines, true );

            return destFolder;
        } catch (Exception e) {
            throw new RuntimeException( String.format("error while writing cloud properties"), e );
        }
	}	
	
    // creates a new pem file for a given hp cloud account.
    private static File createPemFile( CloudProvider cloudProvider, ComputeServiceContext context ){

        //TODO: implement this
        throw new UnsupportedOperationException("unsupported");
//    	File retValue = null;
//
//    	switch( cloudProvider ){
//    	case HP:
//    		try{
//    			CloudBootstrapConfiguration cloudConf = ApplicationContext.get().conf().server.cloudBootstrap;
//    			retValue = HPCloudUtils.createPemFile( context, cloudConf, getTempSuffix() );
//    			}
//    			catch (Exception e) {
//    				throw new RuntimeException(e);
//    			}
//    			break;
//   		default:
//   			throw createNotSupportedCloudRuntimeException( cloudProvider );
//    	}
//
//    	return retValue;
    }
	
	/**
	 * 
	 * Create a security group with all ports open.
	 * 
	 * @param context The jClouds context.
	 */
	public static void createCloudifySecurityGroup( CloudProvider cloudProvider, ComputeServiceContext context ) {

        //TODO: fix this
        throw new UnsupportedOperationException("unsupported");
//		CloudBootstrapConfiguration cloudConf = ApplicationContext.get().conf().server.cloudBootstrap;
//    	switch( cloudProvider ){
//    	case HP:
//    		HPCloudUtils.createCloudifySecurityGroup( context, cloudConf );
//    		break;
//    	case SOFTLAYER:
//    		SoftlayerCloudUtils.createCloudifySecurityGroup( context, cloudConf );
//    		break;
//   		default:
//   			throw createNotSupportedCloudRuntimeException( cloudProvider );
//    	}
	}
	
//    public static CloudApi createCloudApi( ComputeService computeService, CloudProvider cloudProvider, Object cloudRestContextApi ) {
//    	CloudApi cloudApi = null;
//    	switch( cloudProvider ){
//		case HP:
//			cloudApi = new HPCloudApi( computeService, cloudRestContextApi );
//			break;
//
//		default:
//			throw createNotSupportedCloudRuntimeException( cloudProvider );
//    	}
//
//		return cloudApi;
//	}
    
//    public static CloudCreateServerOptions createCloudCreateServerOptions( CloudProvider cloudProvider, Conf conf ) {
//        //TODO Evgeny use right implementation
//    	CloudCreateServerOptions serverOpts = null;
//    	switch( cloudProvider ){
//    		case HP:
//    			serverOpts = new HPCloudCreateServerOptions( conf );
//    			break;
//
//    		default:
//    			throw createNotSupportedCloudRuntimeException( cloudProvider );
//    	}
//
//		return serverOpts;
//	}
    
    private static RuntimeException createNotSupportedCloudRuntimeException( CloudProvider cloudProvider ){
    	return new RuntimeException( "Cloud [" + cloudProvider.name() + "] is not supported" );
    }
    
	public static String getTempSuffix() {
		String currTime = Long.toString(System.currentTimeMillis());
		return currTime.substring(currTime.length() - 4);
	}
}