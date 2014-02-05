package beans.config;

import cloudify.widget.api.clouds.CloudProvider;
import server.ApplicationContext;

public class UiConf {
	
	public CloudProvider getCloudProvider(){
	     return ApplicationContext.get().conf().server.cloudProvider; 
	}
}
