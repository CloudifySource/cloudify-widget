package beans.config;

import server.ApplicationContext;

public class UiConf {
	
	public CloudProvider getCloudProvider(){
	     return ApplicationContext.get().conf().server.cloudProvider; 
	}
}
