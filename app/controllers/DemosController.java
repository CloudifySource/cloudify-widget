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

import models.PublicWidget;
import models.User;

import models.Widget;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import server.ApplicationContext;
import views.html.widgets.demos.userDemoIndex;
import views.html.widgets.demos.userDemoIndexEmbeddable;

import java.util.LinkedList;
import java.util.List;

/**
 * User: guym
 * Date: 2/10/13
 * Time: 3:23 PM
 */
public class DemosController extends Controller {

    public static Result getDemoPageForUser( String email ){
        return ok( userDemoIndex.render( email ) );
    }

    public static Result getEmbeddedDemoPage( String email ){
        if ( email.endsWith( ApplicationContext.get().conf().demoUserEmailSuffix ) ) {
            User user = User.find.where().eq( "email", email ).findUnique();
            if ( user != null ) {
                return ok( userDemoIndexEmbeddable.render( user.getId() ) );
            }
        }

        return ok(  );
    }

    public static Result listWidgetForDemoUser( Long userId ){
        List<Widget> list = new Widget.WidgetQueryConfig().criteria().setEnabled(true).setUser(User.findById(userId)).done().find().findList();
        List<PublicWidget> publicDetails = new LinkedList<PublicWidget>();

        for (Widget widget : list) {
            publicDetails.add(new PublicWidget(widget));
        }

        return ok(Json.toJson(publicDetails));
    }

    public static Result getDemoPageForWidget( Long userId, String apiKey ){
        Widget widget = Widget.getWidget(apiKey);
        if ( widget == null ){
            return badRequest("could not find widget : " + apiKey );
        }
        return ok(views.html.widgets.demos.widgetDemo.render(widget, request().host()));
    }

    public static Result listWidgetForDemoUserByEmail( String email ){
        if ( email.endsWith( ApplicationContext.get().conf().demoUserEmailSuffix )){
            User user = User.find.where().eq( "email" , email ).findUnique() ;
            if ( user != null ){
                return listWidgetForDemoUser( user.getId() );
            }
        }
        return badRequest("can only query for demo users");
    }
}
