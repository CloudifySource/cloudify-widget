package clouds.hp;

import org.jclouds.compute.ComputeService;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;

import clouds.base.CloudApi;
import clouds.base.CloudServerApi;

/**
 * HP implementation of CloudApi
 * @author evgenyf
 * Date: 10/7/13
 */
public class HPCloudApi implements CloudApi{

	private final NovaApi novaApi;
	private final ComputeService computeService;
	
	public HPCloudApi( ComputeService computeService, Object api ){
		this.computeService = computeService;
		if( api instanceof NovaApi ){
			this.novaApi = ( NovaApi )api;
		}
		else{
			throw new RuntimeException( "Api object must be instance of " + 
							NovaApi.class.getName() + "] in the case of HP cloud" );
		}
	}
	

	@Override
	public CloudServerApi getServerApiForZone(String zone) {
		ServerApi novaServerApi = novaApi.getServerApiForZone( zone );
		return new HPCloudServerApi( computeService, novaServerApi );
	}


	@Override
	public String toString() {
		return "HPCloudApi [novaApi=" + novaApi + ", computeService="
				+ computeService + "]";
	}
}