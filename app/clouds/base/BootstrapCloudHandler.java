package clouds.base;

import beans.config.CloudProvider;
import beans.config.Conf;
import models.ServerNode;

public interface BootstrapCloudHandler {
	
	String PARAMS = "params";
	
	void createNewMachine( ServerNode serverNode, Conf conf );
	
	ServerNode bootstrapCloud( ServerNode serverNode, Conf conf );
	
	CloudProvider getCloudProvider();
}
