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

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.MySqlPlatform;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import models.Widget;
import models.WidgetInstance;
import play.Play;
import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;
import server.ApplicationContext;
import server.exceptions.ServerException;

import static utils.RestUtils.*;


/**
 * Widget controller with the main functions like start(), stop(), getWidgetStatus().
 * 
 * @author Igor Goldenberg
 */
public class Application extends Controller
{
    // guy - todo - apiKey should be an encoded string that contains the userId and widgetId.
    //              we should be able to decode it, verify user's ownership on the widget and go from there.
	public static Result start( String apiKey, String hpcsKey, String hpcsSecretKey )
	{
		try
		{
			WidgetInstance wi = ApplicationContext.get().getWidgetServer().deploy(apiKey);
			return resultAsJson(wi);
		}catch(ServerException ex)
		{
			return resultErrorAsJson(ex.getMessage());
		}
	}
	
	
	public static Result stop( String apiKey, String instanceId )
	{
		ApplicationContext.get().getWidgetServer().undeploy(instanceId);

		return ok(OK_STATUS).as("application/json");
	}

	
	public static Result getWidgetStatus( String apiKey, String instanceId )
	{
		try
		{
			Widget.Status wstatus = ApplicationContext.get().getWidgetServer().getWidgetStatus(instanceId);

			return resultAsJson( wstatus );
		}catch(ServerException ex)
		{
			return resultErrorAsJson(ex.getMessage());
		}
	}

    public static Result generateDDL(){
        if ( Play.isDev() ) {
            EbeanServer defaultServer = Ebean.getServer( "default" );

            ServerConfig config = new ServerConfig();
            config.setDebugSql( true );

            DdlGenerator ddlGenerator = new DdlGenerator( ( SpiEbeanServer ) defaultServer, new MySqlPlatform(), config );
            String createDdl = ddlGenerator.generateCreateDdl();
            String dropDdl = ddlGenerator.generateDropDdl();
            return ok( createDdl );
        }else{
            return forbidden(  );
        }
    }

    public static Result javascriptRoutes()
    {
        response().setContentType( "text/javascript" );
        return ok(
                Routes.javascriptRouter( "jsRoutes",

                        // Routes for Projects
                        routes.javascript.WidgetAdmin.getAllWidgets(),
                        routes.javascript.WidgetAdmin.postChangePassword(),
                        routes.javascript.WidgetAdmin.getPasswordMatch(),
                        routes.javascript.WidgetAdmin.deleteWidget()

                )
        );

    }
}