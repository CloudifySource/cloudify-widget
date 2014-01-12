package clouds.hp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jclouds.collect.IterableWithMarker;
import org.jclouds.collect.IterableWithMarkers;
import org.jclouds.collect.PagedIterable;
import org.jclouds.collect.PagedIterables;
import org.jclouds.compute.ComputeService;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.jclouds.openstack.nova.v2_0.options.RebuildServerOptions;

import server.ApplicationContext;
import clouds.base.CloudCreateServerOptions;
import clouds.base.CloudServer;
import clouds.base.CloudServerApi;
import clouds.base.CloudServerCreated;

/**
 * HP implementation of CloudServerApi
 * @author evgenyf
 * Date: 10/7/13
 */
public class HPCloudServerApi implements CloudServerApi {

	private final ServerApi serverApi;
	private final ComputeService computeService;
	
	public HPCloudServerApi( ComputeService computeService, ServerApi serverApi ){
		this.serverApi = serverApi;
		this.computeService = computeService;
	}

	@Override
	public CloudServer get(String serverId) {
		Server server = serverApi.get( serverId );
		if( server != null ){
			return createCloudServer(server);
		}
		
		return null;
	}

	@Override
	public boolean delete(String id) {
		// TODO Auto-generated method stub
		return serverApi.delete(id);
	}

	@Override
	public PagedIterable<CloudServer> listInDetail() {
		PagedIterable<? extends Server> listInDetail = serverApi.listInDetail();
		
		Iterator<?> iterator = listInDetail.iterator();
		List<CloudServer> resultList = new LinkedList<CloudServer>();
		while( iterator.hasNext() ){
			IterableWithMarker iterableWithMarker = ( IterableWithMarker )iterator.next();
			Iterator serversIterator = iterableWithMarker.iterator();
			while( serversIterator.hasNext() ){
				Object obj = serversIterator.next();
				if( obj instanceof Server ){
					resultList.add( createCloudServer( ( Server )obj ) );
				}
			}
		}
		
		IterableWithMarker<CloudServer> iterableWithMarker = IterableWithMarkers.from( resultList );
		PagedIterable<CloudServer> result = PagedIterables.of( iterableWithMarker );
		
		return result;
	}
	
	private static CloudServer createCloudServer( Server server ){
		return new HPCloudServer( server );
	}

	@Override
	public void rebuild( String id ) {
		
		RebuildServerOptions rebuildServerOptions = RebuildServerOptions.Builder.withImage( 
										ApplicationContext.get().conf().server.bootstrap.imageId );
		serverApi.rebuild( id, rebuildServerOptions );
	}

	@Override
	public CloudServerCreated create(String name, String imageRef, String flavorRef, 
															CloudCreateServerOptions... options) {

		List<CreateServerOptions> optionsList = new ArrayList<CreateServerOptions>( options.length );
		for( CloudCreateServerOptions cloudCreateServerOptions :  options ){
			optionsList.add( ( ( HPCloudCreateServerOptions )cloudCreateServerOptions ).getCretaeServerOptions() );
		}
		ServerCreated serverCreated = serverApi.create( name, imageRef, flavorRef, 
							optionsList.toArray( new CreateServerOptions[ optionsList.size() ] ) );
		return new HPCloudServerCreated( serverCreated );
	}

	@Override
	public String toString() {
		return "HPCloudServerApi [serverApi=" + serverApi + "]";
	}
}