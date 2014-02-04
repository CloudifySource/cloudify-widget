package cloudify.widget.api.clouds;


/**
 * 
 * @author evgenyf
 * Date: 10/7/13
 */
public interface CloudServerApi {

	public CloudServer get( String serverId );
	
	public boolean delete(String id);
	
	public PagedIterable<CloudServer> listInDetail();
	
	public void rebuild( String id );
	
	public CloudServerCreated create( String name, String imageRef, String flavorRef, CloudCreateServerOptions... options ) throws RunNodesException;
}
