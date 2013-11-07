package beans.config;

import server.ApplicationContext;

public class UiConf {
	
	public String getCloudProvider(){
	     return ApplicationContext.get().conf().server.cloudProvider; 
	}
}
