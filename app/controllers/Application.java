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

import beans.events.Events;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.MySqlPlatform;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import models.Widget;
import models.WidgetInstance;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Play;
import play.Routes;
import play.i18n.Messages;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;
import server.ApplicationContext;
import server.HeaderMessage;
import server.exceptions.ServerException;

import static utils.RestUtils.*;


/**
 * Widget controller with the main functions like start(), stop(), getWidgetStatus().
 * 
 * @author Igor Goldenberg
 */
public class Application extends Controller
{

    private static Logger logger = LoggerFactory.getLogger(Application.class);
    // guy - todo - apiKey should be an encoded string that contains the userId and widgetId.
    //              we should be able to decode it, verify user's ownership on the widget and go from there.
	public static Result start( String apiKey, String hpcsKey, String hpcsSecretKey, String userId )
	{
		try {
            Widget widget = Widget.getWidget(apiKey);
            if (widget == null || !widget.isEnabled()) {
                logger.info("issuing widget disabled msg for widget: [" + apiKey + "]");
                new HeaderMessage().setError(Messages.get("widget.disabled.by.administrator")).apply(response().getHeaders());
                return badRequest();
            }

            if (widget.isRequiresLogin() && StringUtils.isEmpty(userId)) {
                logger.info("widget requires login yet reached the server");
                new HeaderMessage().setError(Messages.get("widget.requires.login"));
            } else if (!StringUtils.isEmpty(widget.getLoginVerificationUrl())) {
                WS.WSRequestHolder url = WS.url(widget.getLoginVerificationUrl());
                url.setQueryParameter("userId", userId);
                if ( !StringUtils.isEmpty( widget.getWebServiceKey()) ){
                    url.setQueryParameter("webServiceKey", widget.getWebServiceKey());
                }
                logger.info("sending post request to validate login");
                F.Promise<WS.Response> responsePromise = url.post("cloudify widget userId verification");

                WS.Response response = responsePromise.get( ApplicationContext.get().conf().cloudify.verifyLoginUserIdTimeout );
                if (response.getStatus() != OK) {
                    new HeaderMessage().setError(Messages.get("widget.login.verification.failed")).apply(response().getHeaders());
                    return badRequest(response.getBody());
                }
            }

            ApplicationContext.get().getEventMonitor().eventFired( new Events.PlayWidget( request().remoteAddress(), widget ) );
			WidgetInstance wi = ApplicationContext.get().getWidgetServer().deploy(apiKey);
			return resultAsJson(wi);
		}catch(ServerException ex)
		{
			return resultErrorAsJson(ex.getMessage());
		}
	}


	
	
	public static Result stop( String apiKey, String instanceId )
	{
        Widget widget = Widget.getWidget( apiKey );
        if ( widget != null ){
            ApplicationContext.get().getEventMonitor().eventFired( new Events.StopWidget( request().remoteAddress(), widget ) );
        }
        if ( instanceId != null ){
            ApplicationContext.get().getWidgetServer().undeploy(instanceId);
        }

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
                        routes.javascript.WidgetAdmin.checkPasswordStrength(),
                        routes.javascript.WidgetAdmin.postChangePassword(),
                        routes.javascript.WidgetAdmin.getPasswordMatch(),
                        routes.javascript.WidgetAdmin.postRequireLogin(),
                        routes.javascript.WidgetAdmin.deleteWidget()

                )
        );

    }
}