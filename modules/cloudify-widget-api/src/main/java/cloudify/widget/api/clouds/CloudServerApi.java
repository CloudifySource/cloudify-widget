package cloudify.widget.api.clouds;


import java.util.Collection;

/**
 * 
 * @author evgenyf
 * Date: 10/7/13
 */
public interface CloudServerApi {

    /**
     * returns all machines with a specific tag
     *
     * a tag is an identifier of a pool. it can even be a machine name prefix,
     * as long as it can identify machines from specific pool.
     *
     * @param tag -
     * @return all the machines from the pool.
     */
    public Collection<CloudServer> getAllMachinesWithTag( String tag );

    /**
     * get CloudServer by id
     * @param serverId - the server id
     * @return CloudServer
     */
	public CloudServer get( String serverId );

    /**
     * Machine should be removed from the cloud
     * @param id - id of node
     * @return true if were successful, otherwise throw runtime error
     */
	public boolean delete(String id);

    /**
     * rebuild the machine
     * @param id - machine id
     */
	public void rebuild( String id );

    /**
     * create a new machine
     * @param machineOpts  - options for machine
     * @return an instance that holds the new machine's id.
     */
    public CloudServerCreated create( MachineOptions machineOpts );


    @Deprecated // please use create(MachineOptions);
	public CloudServerCreated create( String name, String imageRef, String flavorRef, CloudCreateServerOptions... options ) throws RunNodesException;
}
