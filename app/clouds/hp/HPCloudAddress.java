package clouds.hp;

import org.jclouds.openstack.nova.v2_0.domain.Address;

import clouds.base.CloudAddress;

/**
 * HP implementation of CloudAddress
 * @author evgenyf
 * Date: 10/7/13
 */
public class HPCloudAddress implements CloudAddress{

	private final Address address;
	
	public HPCloudAddress( Address address ){
		this.address = address;
	}

	@Override
	public String getAddr() {
		return address.getAddr();
	}

	@Override
	public String toString() {
		return "HPCloudAddress [address=" + address + "]";
	}
}