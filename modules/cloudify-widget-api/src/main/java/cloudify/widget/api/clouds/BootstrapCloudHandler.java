package cloudify.widget.api.clouds;

public interface BootstrapCloudHandler {
	
	public String PARAMS = "params";

	public void createNewMachine( IServerNode serverNode, IBootstrapCloudConf conf, IComputeServiceContext computeServiceContext );
	
	public IServerNode bootstrapCloud( IServerNode serverNode, IBootstrapCloudConf conf );
	
	public CloudProvider getCloudProvider();
}
