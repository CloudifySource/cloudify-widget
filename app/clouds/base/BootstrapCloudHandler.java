package clouds.base;

import beans.config.CloudProvider;
import beans.config.Conf;
import models.ServerNode;

public interface BootstrapCloudHandler {
	
	public String PARAMS = "params";

	public void createNewMachine( ServerNode serverNode, Conf conf );
	
	public ServerNode bootstrapCloud( ServerNode serverNode, Conf conf );
	
	public CloudProvider getCloudProvider();
}
