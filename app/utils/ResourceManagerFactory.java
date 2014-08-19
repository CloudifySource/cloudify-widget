package utils;

import beans.config.Conf;
import cloudify.widget.common.WidgetResourcesUtils;
import models.ServerNode;
import models.Widget;
import org.springframework.beans.factory.annotation.Autowired;
import server.ApplicationContext;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/10/14
 * Time: 10:08 PM
 */
public class ResourceManagerFactory {

    @Autowired
    private Conf conf;

    public WidgetResourcesUtils.ResourceManager getWidgetRecipeManager( Widget widget ){
        WidgetResourcesUtils.ResourceManager result = new WidgetResourcesUtils.ResourceManager();
        result.setBaseDir( conf.resources.recipesBaseDir.getAbsolutePath() );
        result.setUid( widget.getApiKey() );
        result.setUrl( widget.getRecipeURL() );
        return result;
    }

    public WidgetResourcesUtils.ResourceManager getCloudProviderManager( ServerNode serverNode, String url  ){
        WidgetResourcesUtils.ResourceManager result = new WidgetResourcesUtils.ResourceManager();
        result.setBaseDir( conf.resources.cloudProvidersBaseDir.getAbsolutePath() );
        result.setUid( serverNode.getWidget().getApiKey() );
        result.setUrl( url);
        return result;
    }

    public WidgetResourcesUtils.ResourceManager getCloudProviderManager( Widget widget  ){
        WidgetResourcesUtils.ResourceManager result = new WidgetResourcesUtils.ResourceManager();
        result.setBaseDir( conf.resources.cloudProvidersBaseDir.getAbsolutePath() );
        result.setUid( widget.getApiKey() );
        result.setUrl( widget.getCloudProviderUrl() );
        return result;
    }



    public Conf getConf() {
        return conf;
    }

    public void setConf(Conf conf) {
        this.conf = conf;
    }
}
