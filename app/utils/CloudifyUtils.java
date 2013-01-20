package utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.NovaAsyncApi;
import org.jclouds.openstack.nova.v2_0.domain.KeyPair;
import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
import org.jclouds.rest.RestContext;

import server.exceptions.ServerException;
import beans.config.ServerConfig.CloudBootstrapConfiguration;

public class CloudifyUtils {
	
	/**
	 * Creates a cloud folder containing all necessary credentials for bootstrapping to the HP cloud.
	 * @param cloudConf
	 * @return
	 * @throws IOException
	 */
	public static File createCloudFolder(CloudBootstrapConfiguration cloudConf, String userName, String apiKey) throws IOException {
		File cloudifyEscFolder = new File("D:/GigaSpaces/gigaspaces-cloudify-2.3.0-ga/tools/cli/plugins/esc/");

		//copy the content of hp configuration files to a new folder
		File destFolder = new File(cloudifyEscFolder, cloudConf.cloudName + getTempSuffix()); 
		FileUtils.copyDirectory(new File(cloudifyEscFolder, cloudConf.cloudName), destFolder); 

		// create new pem file using new credentials.
		File pemFolder = new File(destFolder, cloudConf.cloudifyHpUploadDirName);
		File newPemFile = createPemFile(cloudConf, userName, apiKey);
		FileUtils.copyFile(newPemFile, new File(pemFolder, newPemFile.getName() +".pem"), true);//

		String[] userAndTenant = userName.split(":"); 
		List<String> cloudProperties = new ArrayList<String>();
		cloudProperties.add("tenant=" + '"' + userAndTenant[0] + '"');
		cloudProperties.add("user=" + '"' + userAndTenant[1] + '"');
		cloudProperties.add("apiKey=" + '"' + apiKey + '"');
		cloudProperties.add("keyFile=" + '"' + newPemFile.getName() +".pem" + '"');
		cloudProperties.add("keyPair=" + '"' + newPemFile.getName() + '"');
		cloudProperties.add("securityGroup=" + '"' + cloudConf.securityGroup + '"');
		cloudProperties.add("hardwareId=" + '"' + cloudConf.hardwareId + '"');
		cloudProperties.add("linuxImageId=" + '"' + cloudConf.linuxImageId + '"');

		//create new props file and init with custom credentials. 
		File newPropertiesFile = new File(destFolder, cloudConf.cloudPropertiesFileName + ".new" );
		newPropertiesFile.createNewFile();
		FileUtils.writeLines(newPropertiesFile, cloudProperties);

		//delete old props file
		File propertiesFile = new File(destFolder, cloudConf.cloudPropertiesFileName );
		if (propertiesFile.exists()) {
			propertiesFile.delete();
		}

		//rename new props file.
		if (!newPropertiesFile.renameTo(propertiesFile)){
			throw new ServerException("Failed creating custom cloud folder." +
					" Failed renaming custom cloud properties file.");
		}
		return destFolder;
	}
	
	// creates a new pem file for a given hp cloud account.
	private static File createPemFile(CloudBootstrapConfiguration cloudConf, String userName, String apiKey) {
		ComputeServiceContext context = null;
		try {
			Properties overrides = new Properties();
			overrides.put("jclouds.keystone.credential-type", "apiAccessKeyCredentials");
			context = ContextBuilder.newBuilder( cloudConf.cloudProvider )
					.credentials( userName, apiKey )
					.overrides(overrides)
					.buildView(ComputeServiceContext.class);

			// use jClouds to create a new pem file.
			RestContext<NovaApi, NovaAsyncApi> novaClient = context.unwrap();
			NovaApi api = novaClient.getApi();
			KeyPairApi keyPairApi = api.getKeyPairExtensionForZone( cloudConf.zoneName ).get();
			KeyPair keyPair = keyPairApi.create( cloudConf.keyPairName + getTempSuffix()); 

			File pemFile = new File(System.getProperty("java.io.tmpdir"), keyPair.getName());
			pemFile.createNewFile();
			FileUtils.writeStringToFile(pemFile, keyPair.getPrivateKey());
			return pemFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if (context != null) 
				context.close();
		}
	}

	private static String getTempSuffix() {
		String currTime = Long.toString(System.currentTimeMillis());
		return currTime.substring(currTime.length() - 4);
	}

	public static String getCloudPrivateKey(File cloudFolder,
			final CloudBootstrapConfiguration cloudBootstrapConfig) throws IOException {
		File pemFile = getPemFile(cloudFolder, cloudBootstrapConfig);
		if (pemFile == null) {
			return null;
		}
		return FileUtils.readFileToString(pemFile);
	}

	private static File getPemFile(File cloudFolder,
			final CloudBootstrapConfiguration cloudBootstrapConfig) {
		File uploadDir = new File(cloudFolder, cloudBootstrapConfig.cloudifyHpUploadDirName);
		File[] filesList = uploadDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {

				return name.startsWith(cloudBootstrapConfig.keyPairName)
						&& name.endsWith( "pem" );
			}
		});

		if ( filesList.length == 0 || filesList.length > 1) {
			return null;
		}
		return filesList[0];
	}
}
