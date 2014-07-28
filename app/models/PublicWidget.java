package models;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 3/10/14
 * Time: 9:49 PM
 *
 * this class contains all the public information for a widget.
 *
 *
 */
public class PublicWidget{

    Widget widget;

    public PublicWidget(Widget widget){
        this.widget = widget;
    }

    public String getLoginsString() {
        return widget.getLoginsString();
    }

    public String getRecipeName() {
        return widget.getRecipeName();
    }

    public String getConsoleUrlService() {
        return widget.getConsoleUrlService();
    }

    public String getDescription() {
        return widget.getDescription();
    }

    public Boolean getRequireLogin() {
        return widget.getRequireLogin();
    }

    public String getYoutubeVideoUrl() {
        return widget.getYoutubeVideoUrl();
    }

    public String getYoutubeVideoKey() {
        return widget.getYoutubeVideoKey();
    }

    public String getTitle() {
        return widget.getTitle();
    }

    public String getProductVersion() {
        return widget.getProductVersion();
    }

    public String getProviderURL() {
        return widget.getProviderURL();
    }

    public String getProductName() {
        return widget.getProductName();
    }

    public String getApiKey() {
        return widget.getApiKey();
    }

    public String getConsoleURL() {
        return widget.getConsoleURL();
    }

    public String getConsoleName() {
        return widget.getConsoleName();
    }

    public boolean getShowAdvanced() {
        return widget.getShowAdvanced();
    }

    public String getData() { return widget.getData(); }
}
