package clouds.base;

import org.jclouds.collect.PagedIterable;
import org.jclouds.compute.RunNodesException;

/**
 * 
 * @author evgenyf
 * Date: 10/7/13
 */
public interface CloudServerApi {

	CloudServer get( String serverId );
	
	boolean delete(String id);
	
	PagedIterable<CloudServer> listInDetail();
	
	void rebuild( String id );
	
	CloudServerCreated create( String name, String imageRef, String flavorRef, CloudCreateServerOptions... options ) throws RunNodesException;
}
