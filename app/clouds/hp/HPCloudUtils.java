package clouds.hp;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.NovaAsyncApi;
import org.jclouds.openstack.nova.v2_0.domain.Ingress;
import org.jclouds.openstack.nova.v2_0.domain.IpProtocol;
import org.jclouds.openstack.nova.v2_0.domain.KeyPair;
import org.jclouds.openstack.nova.v2_0.domain.SecurityGroup;
import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
import org.jclouds.rest.RestContext;

import beans.config.ServerConfig.CloudBootstrapConfiguration;

import com.google.common.collect.FluentIterable;

/**
 * HP Cloud utility methods
 * @author evgenyf
 * Date: 10/7/13
 */
public class HPCloudUtils {
	
	public static File createPemFile( ComputeServiceContext context, CloudBootstrapConfiguration cloudConf, String tempSuffix ) throws IOException{
		
		RestContext<NovaApi, NovaAsyncApi> novaClient = context.unwrap();
        NovaApi api = novaClient.getApi();
        KeyPairApi keyPairApi = api.getKeyPairExtensionForZone( cloudConf.zoneName ).get();
		KeyPair keyPair = keyPairApi.create( cloudConf.keyPairName + tempSuffix );

        File pemFile = new File(System.getProperty("java.io.tmpdir"), keyPair.getName());
		pemFile.createNewFile();
		FileUtils.writeStringToFile(pemFile, keyPair.getPrivateKey());
		
		return pemFile;
	}
	
	
	public static void createCloudifySecurityGroup( ComputeServiceContext context, CloudBootstrapConfiguration cloudConf ) {

		try {
			RestContext<NovaApi, NovaAsyncApi> novaClient = context.unwrap();
			NovaApi novaApi = novaClient.getApi();
			SecurityGroupApi securityGroupClient = novaApi.getSecurityGroupExtensionForZone(cloudConf.zoneName).get();
			//Check if group already exists.
			FluentIterable<? extends SecurityGroup> groupsList = securityGroupClient.list();
			for (Object group : groupsList) {
				if (((SecurityGroup)group).getName().equals(cloudConf.securityGroup)) {
					return;
				}
			}
			//Create a new security group with open port range of 80-65535.
			Ingress ingress = Ingress.builder().ipProtocol(IpProtocol.TCP).fromPort(1).toPort(65535).build();
			SecurityGroup securityGroup = securityGroupClient.createWithDescription(cloudConf.securityGroup, "All ports open.");
			securityGroupClient.createRuleAllowingCidrBlock(securityGroup.getId(), ingress, "0.0.0.0/0");
		} 
		catch (Exception e) {
			throw new RuntimeException("Failed creating security group.", e);
		} 
	}
}
