/*******************************************************************************
 * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package beans;

import static server.Config.WIDGET_STOP_TIMEOUT;

import java.io.*;
import java.util.List;

import controllers.WidgetAdmin;

import play.cache.Cache;
import play.mvc.Controller;

import models.ServerNode;
import models.Widget;
import models.Widget.Status;
import models.WidgetInstance;
import server.*;

import javax.inject.Inject;

/**
 * This class provides ability to deploy/undeploy new widget by apiKey.
 * Before that the user must create an account by WidgetAdmin and register a new widget.
 * 
 * @author Igor Goldenberg
 * @see ServerPoolImpl
 * @see WidgetAdmin
 */
public class WidgetServerImpl implements WidgetServer
{
    @Inject
    private ServerPool serverPool;

    @Inject
    private DeployManager deployManager;
	/**
	 * Deploy a widget instance.
	 * @param apiKey 
	 */
	public WidgetInstance deploy( String apiKey )
	{
		// don't allow for 30 seconds to start the widget again
		Long timeLeft = (Long)Cache.get(Controller.request().remoteAddress());
		if ( timeLeft != null )
			throw new ServerException(ResMessages.getFormattedString("please_wait_x_sec", (timeLeft - System.currentTimeMillis()) / 1000));

		Widget widget = Widget.getWidgetByApiKey(apiKey);
		if ( !widget.isEnabled() )
			throw new ServerException("widget_disabled_by_administrator");
			
		File recipeDir = Utils.downloadAndUnzip(widget.getRecipeURL(), apiKey);

		ServerNode server = serverPool.get();
		if ( server == null )
			throw new ServerException(ResMessages.getString("no_available_servers"));
		
		widget.countLaunch();
		
		String instanceId = deployManager.fork(server, recipeDir).getId();
		
		return widget.addWidgetInstance( instanceId, server.getPublicIP() );
	}
	
	
	public void undeploy( String instanceId )
	{
		// keep the user for 30 seconds by IP, to avoid immediate widget start after stop
		Cache.set(Controller.request().remoteAddress(), new Long(System.currentTimeMillis() + WIDGET_STOP_TIMEOUT*1000), WIDGET_STOP_TIMEOUT ); 
		
		serverPool.destroy(instanceId);
	}

	
	public Status getWidgetStatus( String instanceId )
	{
		ProcExecutor pe = deployManager.getExecutor(instanceId);
		if ( pe == null )
			return new Status(Status.STATE_STOPPED, ResMessages.getString("server_was_terminated"));
		
		List<String> output = Utils.formatOutput(pe.getOutput(), pe.getPrivateServerIP() + "]");
		Status wstatus = new Status(Status.STATE_RUNNING, output, pe.getElapsedTimeMin());
		
		return wstatus;
	}

    public void setServerPool(ServerPool serverPool) {
        this.serverPool = serverPool;
    }

    public void setDeployManager(DeployManager deployManager) {
        this.deployManager = deployManager;
    }
}