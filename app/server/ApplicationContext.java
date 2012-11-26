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
package server;


/**
 * A singleton class that helps to get an instance of different modules and keeps loose decoupling.
 * 
 * @author Igor Goldenberg
 */
public class ApplicationContext
{
	static private DeployManager procManager;
	static private WidgetServer widgetServer;
	static private ServerPool serverPool;
	static private ServerBootstrapper serverBootstrapper;
	static private ExpireServersCollector expiredServerCollector;
	
	public static synchronized DeployManager getDeployManager()
	{
		if ( procManager == null )
			procManager = new DeployManager();
		
		return procManager;
	}
	
	public static synchronized WidgetServer getWidgetServer()
	{
		if ( widgetServer == null )
			widgetServer = new WidgetServer();
		
		return widgetServer;
	}
	
	public static synchronized ServerPool getServerPool()
	{
		if ( serverPool == null )
			serverPool = new ServerPool();
		
		return serverPool;
	}

	public static synchronized ServerBootstrapper getServerBootstrapper()
	{
		if ( serverBootstrapper == null )
			serverBootstrapper = new ServerBootstrapper();
		
		return serverBootstrapper;
	}
	
	public static synchronized ExpireServersCollector getExpiredServersCollector()
	{
		if ( expiredServerCollector == null )
			expiredServerCollector = new ExpireServersCollector();
		
		return expiredServerCollector;
	}
}
