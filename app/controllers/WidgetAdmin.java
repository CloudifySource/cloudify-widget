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

import java.util.List;
import java.util.Map;

import models.ServerNode;
import models.Summary;
import models.User;
import models.Widget;
import models.WidgetInstance;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import server.ApplicationContext;
import server.Config;
import server.ServerException;
import server.Utils;
import static controllers.RestUtils.*;


/**
 * Widget Admin controller.
 * 
 * @author Igor Goldenberg
 */
public class WidgetAdmin extends Controller
{
	/**
	 * Creates new account.
	 * 
	 * @param email
	 * @param password
	 * @return
	 */
	public static Result signUp( String email, String password, String firstname, String lastname )
	{
		try
		{
			User.Session session = User.newUser(email, password).getSession();

			return RestUtils.resultAsJson(session);
		}catch( ServerException ex )
		{
			return resultErrorAsJson(ex.getMessage());
		}
	}

    public static Result index()
    {
        // lets assume that if we have "authToken" we are already logged in
        // and we can redirect to widgets.html
        Http.Cookie authToken = request().cookies().get( "authToken" );
        if ( authToken != null && User.validateAuthToken( authToken.value(), true ) != null ) {
            return redirect( "/admin/widgets.html" );
        }
        else{
            return redirect( "/admin/signin.html" );
        }
    }

	/**
	 * Login with existing account.
	 * 
	 * @param email
	 * @param password
	 * @return
	 */
	public static Result signIn( String email, String password )
	{
		try
		{
			User.Session session = User.authenticate(email, password);

			return resultAsJson(session);
		}catch( ServerException ex )
		{
			return resultErrorAsJson(ex.getMessage());
		}
	}

	
	public static Result getAllUsers( String authToken )
	{
		User.validateAuthToken(authToken);
		List<User> users = User.getAllUsers();

		return resultAsJson(users);
	}


	public static Result createNewWidget( String authToken,  String productName, String productVersion,
										  String title, String youtubeVideoUrl, String providerURL,
										  String recipeURL, String consolename, String consoleurl )
	{
		User user = User.validateAuthToken(authToken);
		Widget widget = user.createNewWidget( productName, productVersion, title, youtubeVideoUrl, providerURL, recipeURL, consolename, consoleurl );
		
		return resultAsJson(widget);
	}

	
	public static Result getAllWidgets( String authToken )
	{
		User user = User.validateAuthToken(authToken);
		List<Widget> list = Utils.workaround(user.getWidgets());

		return resultAsJson(list);
	}
	
	
	public static Result getAllServers()
	{
		List<ServerNode> list = ServerNode.find.all();

		return resultAsJson(list);
	}

	
	public static Result shutdownInstance( String authToken, String instanceId )
	{
		User.validateAuthToken(authToken);
		ApplicationContext.getWidgetServer().undeploy(instanceId);
		
		return ok(OK_STATUS).as("application/json");
	}
	

	public static Result disableWidget( String authToken, String apiKey )
	{
		User.validateAuthToken(authToken);
		Widget widget = Widget.getWidgetByApiKey( apiKey );
		widget.setEnabled( false );
		
		return ok(OK_STATUS).as("application/json");
	}
	

	public static Result enableWidget( String authToken, String apiKey )
	{
		User.validateAuthToken(authToken);

		Widget widget = Widget.getWidgetByApiKey( apiKey );
		widget.setEnabled( true );
		
		return ok(OK_STATUS).as("application/json");
	}
	
	
	public static Result summary( String authToken )
	{
		User.validateAuthToken(authToken);

		Summary summary = new Summary();

		int totalUsers = User.find.findRowCount();
		int totalWidgets = Widget.find.findRowCount();
		int totalInstances = WidgetInstance.find.findRowCount();
		int totalIdleServers = ServerNode.find.where().eq("busy", "false").findRowCount();
		int totalBusyServers = ServerNode.find.where().eq("busy", "true").findRowCount();

		summary.addAttribute("Users", String.valueOf( totalUsers ));
		summary.addAttribute("Widgets", String.valueOf( totalWidgets ));
		summary.addAttribute("Instances", String.valueOf( totalInstances ));
		summary.addAttribute("Idle Servers", String.valueOf( totalIdleServers ));
		summary.addAttribute("Busy Servers", String.valueOf( totalBusyServers ));


		return resultAsJson(summary);
	}
	
	public static Result regenerateWidgetApiKey( String authToken, String apiKey )
	{
		User.validateAuthToken(authToken);

		Widget widget = Widget.regenerateApiKey(apiKey);
		
		return resultAsJson(widget);
	}

	public static Result headers()
	{
    	Http.Request req = Http.Context.current().request();
    	
    	StringBuilder sb = new StringBuilder("HEADERS:");
    	sb.append( "\nRemote address: " + req.remoteAddress() );
    	
    	Map<String, String[]> headerMap = req.headers();
    	for (String headerKey : headerMap.keySet()) 
    	{
    	    for( String s : headerMap.get(headerKey) )
    	    	sb.append( "\n" + headerKey + "=" + s);
    	}

    	return ok(sb.toString());
	}
}