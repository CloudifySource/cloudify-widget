package controllers;

import cloudify.widget.common.WidgetResourcesUtils;
import models.User;
import models.Widget;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import server.ApplicationContext;
import utils.ResourceManagerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/17/14
 * Time: 6:10 PM
 */
public class WidgetResources extends GsController {


    static public Result getRecipeResourceDetails( Long widgetId ){

        Widget w = Widget.findByUserAndId( validateSession(), widgetId );

        ResourceManagerFactory resourceManagerFactory = ApplicationContext.get().getResourceManagerFactory();
        WidgetResourcesUtils.ResourceManager widgetRecipeManager = resourceManagerFactory.getWidgetRecipeManager(w);

        ResourceStatusResult result = new ResourceStatusResult();
        result.downloaded = widgetRecipeManager.isExtracted();
        result.lastModified = widgetRecipeManager.lastModified();

        return ok(Json.toJson(result));

    }

    static public Result getWalkRecipeResourceResult( Long widgetId ){

        Widget w = Widget.findByUserAndId(validateSession(),widgetId);

        ResourceManagerFactory resourceManagerFactory = ApplicationContext.get().getResourceManagerFactory();
        WidgetResourcesUtils.ResourceManager widgetRecipeManager = resourceManagerFactory.getWidgetRecipeManager(w);

        return ok ( Json.toJson( widgetRecipeManager.walk() ));

    }

    static public Result postManualRefreshResource( Long widgetId ){


        Widget w = Widget.findByUserAndId(validateSession(),widgetId);

        ResourceManagerFactory resourceManagerFactory = ApplicationContext.get().getResourceManagerFactory();
        WidgetResourcesUtils.ResourceManager widgetRecipeManager = resourceManagerFactory.getWidgetRecipeManager(w);

        widgetRecipeManager.delete();
        widgetRecipeManager.download();
        widgetRecipeManager.extract();

        return getRecipeResourceDetails( widgetId );
    }

    static public Result getResourceContent( Long widgetId, String path ){
        User user = validateSession();
        Widget w = Widget.findByUserAndId(user,widgetId);

        ResourceManagerFactory resourceManagerFactory = ApplicationContext.get().getResourceManagerFactory();
        WidgetResourcesUtils.ResourceManager widgetRecipeManager = resourceManagerFactory.getWidgetRecipeManager(w);

        return ok( widgetRecipeManager.read( path ) );
    }


    static public Result getProviderResourceDetails( Long widgetId ){
        User user = validateSession();
        Widget w = Widget.findByUserAndId( user, widgetId );

        ResourceManagerFactory resourceManagerFactory = ApplicationContext.get().getResourceManagerFactory();
        WidgetResourcesUtils.ResourceManager widgetRecipeManager = resourceManagerFactory.getWidgetRecipeManager(w);

        ResourceStatusResult result = new ResourceStatusResult();
        result.downloaded = widgetRecipeManager.isExtracted();
        result.lastModified = widgetRecipeManager.lastModified();

        return ok(Json.toJson(result));

    }

    static public Result getWalkProviderResourceResult( Long widgetId ){

        Widget w = Widget.findByUserAndId(validateSession(),widgetId);

        ResourceManagerFactory resourceManagerFactory = ApplicationContext.get().getResourceManagerFactory();
        WidgetResourcesUtils.ResourceManager widgetRecipeManager = resourceManagerFactory.getWidgetRecipeManager(w);

        return ok ( Json.toJson( widgetRecipeManager.walk() ));

    }

    static public Result postManualRefreshProvider( Long widgetId ){

        User user = validateSession();
        Widget w = Widget.findByUserAndId(user,widgetId);

        ResourceManagerFactory resourceManagerFactory = ApplicationContext.get().getResourceManagerFactory();
        WidgetResourcesUtils.ResourceManager widgetRecipeManager = resourceManagerFactory.getCloudProviderManager(w);

        widgetRecipeManager.delete();
        widgetRecipeManager.download();
        widgetRecipeManager.extract();

        return getRecipeResourceDetails( widgetId );
    }

    static  public Result getProviderContent( Long widgetId, String path ){

        Widget w = Widget.findByUserAndId(validateSession(),widgetId);

        ResourceManagerFactory resourceManagerFactory = ApplicationContext.get().getResourceManagerFactory();
        WidgetResourcesUtils.ResourceManager widgetRecipeManager = resourceManagerFactory.getWidgetRecipeManager(w);

        return ok( widgetRecipeManager.read( path ) );
    }

    public static class ResourceStatusResult{
        public long lastModified;
        public boolean downloaded;
    }
}
