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
package controllers;

import models.Widget;
import models.WidgetInstance;
import play.mvc.Controller;
import play.mvc.Result;
import server.ApplicationContext;
import server.ServerException;
import static controllers.RestUtils.*;


/**
 * Widget controller with the main functions like start(), stop(), getWidgetStatus().
 * 
 * @author Igor Goldenberg
 */
public class Application extends Controller
{
	public static Result start( String apiKey, String hpcsKey, String hpcsSecretKey )
	{
		try
		{
			WidgetInstance wi = ApplicationContext.getWidgetServer().deploy(apiKey);
			
			return resultAsJson(wi);
		}catch(ServerException ex)
		{
			return resultErrorAsJson(ex.getMessage());
		}
	}
	
	
	public static Result stop( String apiKey, String instanceId )
	{
		ApplicationContext.getWidgetServer().undeploy(instanceId);

		return ok(OK_STATUS).as("application/json");
	}

	
	public static Result getWidgetStatus( String apiKey, String instanceId )
	{
		try
		{
			Widget.Status wstatus = ApplicationContext.getWidgetServer().getWidgetStatus(instanceId);

			return resultAsJson( wstatus );
		}catch(ServerException ex)
		{
			return resultErrorAsJson(ex.getMessage());
		}
	}
}