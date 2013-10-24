package clouds.hp;

import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;

import clouds.base.CloudServerCreated;

/**
 * HP implementation of CloudServerCreated
 * @author evgenyf
 * Date: 10/7/13
 */
public class HPCloudServerCreated implements CloudServerCreated {

	private final ServerCreated serverCreated;
	
	public HPCloudServerCreated( ServerCreated serverCreated ){
		this.serverCreated = serverCreated;
	}
	
	@Override
	public String getId() {
		return serverCreated.getId();
	}

	@Override
	public String toString() {
		return "HPCloudServerCreated [serverCreated=" + serverCreated + "]";
	}
}