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

import play.modules.spring.Spring;

/**
 * A singleton class that helps to get an instance of different modules and keeps loose decoupling.
 *
 * @author Igor Goldenberg
 */
public class ApplicationContext
{
    public static final String PROC_MANAGER = "procManager";
    public static final String WIDGET_SERVER = "widgetServer";
    public static final String SERVER_POOL = "serverPool";
    public static final String SERVER_BOOTSTRAPPER = "serverBootstrapper";
    public static final String EXPIRED_SERVER_COLLECTOR = "expiredServerCollector";

    public static DeployManager getDeployManager()
	{
        return (DeployManager) Spring.getBean(PROC_MANAGER);

	}

	public static WidgetServer getWidgetServer()
	{
        return (WidgetServer) Spring.getBean(WIDGET_SERVER);
	}

	public static ServerPool getServerPool()
	{
        return (ServerPool) Spring.getBean(SERVER_POOL);
    }

	public static ServerBootstrapper getServerBootstrapper()
	{
		return (ServerBootstrapper) Spring.getBean(SERVER_BOOTSTRAPPER);
	}

	public static ExpiredServersCollector getExpiredServersCollector()
	{
		return (ExpiredServersCollector) Spring.getBean(EXPIRED_SERVER_COLLECTOR);
	}
}
