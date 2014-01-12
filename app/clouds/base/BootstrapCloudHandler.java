package clouds.base;

import org.jclouds.compute.ComputeServiceContext;

import beans.config.CloudProvider;
import beans.config.Conf;
import models.ServerNode;

public interface BootstrapCloudHandler {
	
	public String PARAMS = "params";

	public void createNewMachine( ServerNode serverNode, Conf conf, ComputeServiceContext computeServiceContext );
	
	public ServerNode bootstrapCloud( ServerNode serverNode, Conf conf, ComputeServiceContext computeServiceContext );
	
	public CloudProvider getCloudProvider();
}
