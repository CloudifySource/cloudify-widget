/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package controllers;

import com.avaje.ebean.Ebean;
import models.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import server.ApplicationContext;
import server.HeaderMessage;
import utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.RestUtils.OK_STATUS;


/**
 * Widget Admin controller.
 * 
 * @author Igor Goldenberg
 */
public class WidgetAdmin extends GsController
{

    private static Logger logger = LoggerFactory.getLogger( WidgetAdmin.class );

    public static Result getWidget( String apiKey ){
        if ( StringUtils.isEmpty(apiKey)){
            return badRequest("apiKey required");
        }
        Widget widgetItem = Widget.getWidget(apiKey);
        if ( widgetItem == null ){//|| !widgetItem.isEnabled()){
            return ok();
        }
        return redirect("/public-folder/angularApps/index.html#/widgets/" + apiKey + "/view?since=" + System.currentTimeMillis());
    }

    public static Result icon( String apiKey ){
        WidgetIcon widgetItem = WidgetIcon.findByWidgetApiKey( apiKey );
        if ( widgetItem == null || ArrayUtils.isEmpty(widgetItem.getData()) ){
            return ok().as( "image/png");
//            return notFound(  );
        }
        return ok( widgetItem.getData() ).as( widgetItem.getContentType() );
    }




    /**
     * This function will save the widget.
     * It receives the user's authToken, the widget in JSON format, and icon file in the request body.
     *
     * This method handles 2 scenarios (not best practice, we know),
     *  - if widget exists
     *  - if widget does not exists - we create it
     *
     *
     * Removing an icon is decided if form gets "removeIcon" key.
     * @return the widget as JSON - without the icon data.
     */
    public static Result postWidget( ){

        // read everything from the form.
        Http.MultipartFormData body = request().body().asMultipartFormData();
        String widgetString = body.asFormUrlEncoded().get( "widget" )[0];
        boolean removeIcon = body.asFormUrlEncoded().containsKey("removeIcon");
        String authToken = session("authToken");
        Http.MultipartFormData.FilePart picture = body.getFile( "icon" );

        JsonNode jsonNode = Json.parse(widgetString);
        Widget w = null;
        User user = null;
        if ( jsonNode.has( "apiKey" ) &&  !jsonNode.get("apiKey").isNull() && StringUtils.isNotEmpty(jsonNode.get("apiKey").getTextValue()) ){
            String widgetApiKey = jsonNode.get("apiKey").asText();
            w = getWidgetSafely( authToken, widgetApiKey );
        }else{
            user = User.validateAuthToken( authToken );
        }

        ObjectMapper mapper = Utils.getObjectMapper();

        Form<Widget> validator = form( Widget.class ).bind( jsonNode );

        // ignore apiKey errors
        validator.errors().remove("apiKey");

        if ( validator.hasErrors() ){
            new HeaderMessage().populateFormErrors( validator ).apply( response().getHeaders() );
            logger.error("trying to save an invalid widget " + validator.toString());
            return badRequest( validator.errorsAsJson() );
        }

        try {
            if ( w == null ){
                // creating a new widget.
                w = mapper.treeToValue( jsonNode, Widget.class );
                w.init();
            }else{
                mapper.readerForUpdating( w ).treeToValue( jsonNode, Widget.class );
            }
            logger.info( "successfully turned json to widget [{}]", w );

            if ( user != null ){
                user.addNewWidget( w );
            }

            if ( w.mailChimpDetails != null ){
                if ( w.mailChimpDetails.getId() != null ) {
                    w.mailChimpDetails.update();
                }
                w.mailChimpDetails.save();
            }



            w.update();
            w.save(  );
            if ( w.installFinishedEmailDetails != null ){
                w.installFinishedEmailDetails.setWidget( w );
                if ( w.installFinishedEmailDetails.getId() != null ){
                    w.installFinishedEmailDetails.update();
                }
                w.installFinishedEmailDetails.save();
            }

            if ( w.getAwsImageShare() != null ){
                w.getAwsImageShare().setWidget(w);
                if ( w.getAwsImageShare().getId() != null ){
                    w.getAwsImageShare().update();
                }
                w.getAwsImageShare().save();
            }


            w.refresh( );

            if ( removeIcon ){
                WidgetIcon icon = WidgetIcon.findByWidgetApiKey( w.getApiKey() );
                if ( icon != null ){
                    w.setIcon(null);
                    w.save();
                    icon.delete();
                }
            }

            // now lets handle the icon - but only if one was posted.
            if ( picture != null ) {

                // decide if widget already has an icon or not
                WidgetIcon icon = WidgetIcon.findByWidgetApiKey( w.getApiKey() );

                if ( icon == null ){
                    icon = new WidgetIcon();
                }
                String fileName = picture.getFilename();
                String contentType = picture.getContentType();

                File file = picture.getFile();
                byte[] iconData = IOUtils.toByteArray( new FileInputStream( file ) );

                icon.setName( fileName );
                icon.setContentType( contentType );
                icon.setData( iconData );
                Ebean.save( icon ); // supports both save and update.
                w.setIcon( icon );
                w.save(  );
                return ok( "Added icon successfully" );
            }


            return ok(  Json.toJson( w ) );
        } catch ( IOException e ) {
            logger.error( "unable to turn body to Json",e  );
        }
        logger.info( "saving widget [{}]", widgetString );
        return ok(  );
    }


    // we want to return default values for the widget
    public static Result getWidgetDefaultValues(){
        return ok(Json.toJson(new Widget()));
    }

//

	
	public static Result getAllWidgets(  )
	{
		User user = User.validateAuthToken(session("authToken"));
		List<Widget> list = null;

        if ( user.getSession().isAdmin() )   {
            list = Widget.find.all(); // Utils.workaround( Widget.find.all() );
//            list = Utils.workaround( Widget.find.all() );
        }
        else {
            list = user.getWidgets();
        }


        ObjectMapper mapper = Utils.getObjectMapper();
        mapper.getSerializationConfig().addMixInAnnotations( Widget.class, Widget.IncludeInstancesMixin.class );
        return ok( Json.toJson(list) );
	}



	public static Result shutdownInstance( String authToken, String instanceId )
	{
		User.validateAuthToken(authToken);
		ApplicationContext.get().getWidgetServer().undeploy( ServerNode.getServerNode( instanceId )); // todo : link to user somehow
		return ok(OK_STATUS).as("application/json");
	}

    public static Result disableWidgetById( Long widgetId )
    {
        return enableDisableWidget( widgetId, false );
    }

    private static Result enableDisableWidget( Long widgetId, boolean enabled )
    {
        getWidgetSafely( session("authToken"), widgetId, true ).setEnabled( enabled ).save();
        return ok(OK_STATUS).as("application/json");
    }

    public static Result enableWidgetById( Long widgetId )
	{
        return enableDisableWidget( widgetId, true );
	}

    private static Widget getWidgetSafely( String authToken, Long widgetId, boolean allowAdmin ){
        User user = User.validateAuthToken( authToken );
        if ( allowAdmin && user.isAdmin()){
            return Widget.find.byId( widgetId );
        }else{
            return Widget.findByUserAndId( user, widgetId );
        }
    }

    private static Widget getWidgetSafely( String authToken, String apiKey )
    {

        User user = User.validateAuthToken(authToken);
        if ( user.isAdmin() ){
             return Widget.getWidget(apiKey);
        }else{
            return Widget.getWidgetByApiKey(user, apiKey);
        }
    }


    public static Result getWidgetById( Long widgetId ){
        return ok(Json.toJson(getWidgetSafely(session("authToken"), widgetId, false)));
    }

    public static Result deleteWidgetById( Long widgetId ){
        String authToken = session("authToken");
        Widget widget = getWidgetSafely( authToken, widgetId, true );
        widget.delete(  );
        return ok( );
    }


	
	public static Result regenerateWidgetApiKey( String authToken, String apiKey )
	{
        Widget w = getWidgetSafely( authToken, apiKey ).regenerateApiKey();
        logger.info( "regenerated api key to [{}]", w );
        Map<String, Object> result = new HashMap<String, Object>(  );
        result.put("widget", w);
		return ok( Json.toJson( result ) );
	}




    public static Result getPublicWidgetDetails( String apiKey ){
        Widget w = Widget.getWidget(apiKey);
        return ok(Json.toJson(new PublicWidget(w)));
    }


}