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
package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;


import com.avaje.ebean.Expr;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.codehaus.jackson.annotate.JsonBackReference;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.expression.spel.ExpressionState;
import play.db.ebean.Model;
import play.i18n.Messages;
import server.exceptions.ServerException;
import utils.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import controllers.WidgetAdmin;

/**
 * This class represents a widget metadata and relates to a specific {@link User}.
 * See {@link WidgetAdmin#createNewWidget(String, String, String, String, String, String, String, String, String)}
 * 
 * @author Igor Goldenberg
 * @see WidgetAdmin
 */
@Entity
@XStreamAlias("widget")
@SuppressWarnings("serial")
public class Widget
	extends Model
{
	@Id
	private Long id;
	private String productName;
	private String providerURL;
	private String productVersion;
	private String title;
	private String youtubeVideoUrl;
	private String recipeURL;
	private Boolean allowAnonymous;
	private String apiKey;
	private Integer launches;
	private Boolean enabled;
    @JsonProperty(value="consolename")
	private String consoleName;
    @JsonProperty(value="consoleurl")
	private String consoleURL;
    @JsonProperty( value="rootpath")
    private String recipeRootPath;
    private Boolean requireLogin = false; // should this widget support login?
    private String loginVerificationUrl = null; // url to verify user IDs.
    private String webServiceKey=null; // secret key we add on the web service calls.

    @ManyToOne( optional = false )
    private User user;




	@OneToMany(cascade=CascadeType.ALL) 
	private List<WidgetInstance> instances;

	public static Finder<Long,Widget> find = new Finder<Long,Widget>(Long.class, Widget.class);





    /** This class serves as status of the widget instance  */
	@XStreamAlias("status")
	final static public class Status
	{
		public final static String STATE_STOPPED = "stopped";
		public final static String STATE_RUNNING = "running";
		
		private String state;
		private List<String> output;
		private Integer timeleft;
		
		public Status( String state, String...messages )
		{
			this.state = state;
			output = new ArrayList<String>();
            Collections.addAll( output, messages );
		}
		
		public Status( String state, List<String> output, int timeleftMin )
		{
			this.state = state;
			this.output = output;
			
			if ( output.isEmpty() )
				output.add( Messages.get("wait.while.preparing.env"));
				
			this.timeleft = timeleftMin == 0 ? 1 : timeleftMin; // since we show in minutes, the latest minute show always 1 minute.
		}
	}
	
	public Widget( String productName, String productVersion, String title, String youtubeVideoUrl,
					String providerURL, String recipeURL, String consoleName, String consoleURL, String recipeRootPath )
	{
		this.productName = productName;
		this.title = title;
		this.productVersion = productVersion;
		this.youtubeVideoUrl = youtubeVideoUrl;
		this.providerURL = providerURL;
		this.recipeURL  = recipeURL;
		this.consoleName = consoleName;
		this.consoleURL = consoleURL;
		this.enabled = true;
		this.launches = 0;
		this.apiKey = UUID.randomUUID().toString();
        this.recipeRootPath = recipeRootPath;
	}
	
	public WidgetInstance addWidgetInstance( String instanceId, String publicIP )
	{
		WidgetInstance wInstance = new WidgetInstance( instanceId, publicIP, consoleName, consoleURL );
		
		if (instances == null){
			instances = new ArrayList<WidgetInstance>();
        }

		instances.add( wInstance );
		
		save();
		
		return wInstance;
	}

    @Deprecated // todo : DO NOT USE THIS.. this is strictly for "WidgetServerImpl".
               //  todo : we should implement a different mechanism there, but for now there is no time.
    public static Widget getWidget( String apiKey )
    {
        Widget widget = Widget.find.where().eq( "apiKey", apiKey ).findUnique();
        if ( widget == null ) {
            String msg = Messages.get( "widget.apikey.not.valid", apiKey );
            throw new ServerException( msg ).getResponseDetails().setError( msg ).done();
        }
        return widget;
    }


    public static Widget findByUserAndId( User user, Long widgetId )
    {
        return find.where( ).eq( "user", user ).eq( "id",widgetId ).findUnique();
    }
    /** @return the widget by apiKey or null  */
    // guy - NOTE : we must always add "user" to the mix.. otherwise we never verify the user really owns the widget.
	static public Widget getWidgetByApiKey( User user, String apiKey )
	{
		Widget widget = Widget.find.where().eq( "apiKey", apiKey ).eq( "user", user).findUnique();

		if ( widget == null ) {
            String msg = Messages.get( "widget.apikey.not.valid", apiKey );
            throw new ServerException( msg ).getResponseDetails().setError( msg ).done();
        }
		
		return widget;
	}
	
	static public Widget regenerateApiKey( User user, String oldApiKey )
	{
		Widget widget = getWidgetByApiKey( user, oldApiKey );
		widget.apiKey = UUID.randomUUID().toString();
		widget.save();
		
		widget.setInstances(null);
		
		return widget;
	}
	
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}



	public String getApiKey()
	{
		return apiKey;
	}

	public void setApiKey(String apiKey)
	{
		this.apiKey = apiKey;
	}

	public List<WidgetInstance> getInstances()
	{
		return instances;
	}

	public void setInstances(List<WidgetInstance> instances)
	{
		this.instances = instances;
	}

	public int getLaunches()
	{
		return launches;
	}

    public void setConsoleName( String consoleName )
    {
        this.consoleName = consoleName;
    }

    public void setConsoleURL( String consoleURL )
    {
        this.consoleURL = consoleURL;
    }

    public void setRecipeRootPath( String recipeRootPath )
    {
        this.recipeRootPath = recipeRootPath;
    }

    public void setLaunches(int launches)
	{
		this.launches = launches;
	}

	public String getRecipeURL()
	{
		return recipeURL;
	}

	public void setRecipeURL(String recipeURL)
	{
		this.recipeURL = recipeURL;
	}

	public String getProductName()
	{
		return productName;
	}

	public void setProductName(String productName)
	{
		this.productName = productName;
	}

	public String getProviderURL()
	{
		return providerURL;
	}

	public void setProviderURL(String providerURL)
	{
		this.providerURL = providerURL;
	}

	public String getProductVersion()
	{
		return productVersion;
	}

	public void setProductVersion(String productVersion)
	{
		this.productVersion = productVersion;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getYoutubeVideoUrl()
	{
		return youtubeVideoUrl;
	}

	public void setYoutubeVideoUrl(String youtubeVideoUrl)
	{
		this.youtubeVideoUrl = youtubeVideoUrl;
	}

	public Boolean getAllowAnonymous()
	{
		return allowAnonymous;
	}

	public void setAllowAnonymous(Boolean allowAnonymous)
	{
		this.allowAnonymous = allowAnonymous;
	}

	public void setLaunches(Integer launches)
	{
		this.launches = launches;
	}
	
	public Boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(Boolean enabled)
	{
		this.enabled = enabled;
		save();
	}

    public String getRecipeRootPath()
    {
        return recipeRootPath;
    }

    public void countLaunch()
	{
		launches++;
		save();
	}
	
	@Override
	public String toString()
	{
		return Utils.reflectedToString(this);
	}


    public String toDebugString() {
        return "Widget{" +
                "id=" + id +
                ", productName='" + productName + '\'' +
                ", title='" + title + '\'' +
                ", enabled=" + enabled +
                ", apiKey='" + apiKey + '\'' +
                ", user=" + user.toDebugString() +
                '}';
    }


    @JsonBackReference
//    @JsonProperty
    public User getUser() {
        return user;
    }

    // guy - for display properties only!
    @JsonProperty
    public String getUsername(){
        return user.getEmail();
    }


    public boolean isRequiresLogin() {
        return requireLogin == Boolean.TRUE; // solves NPE
    }

    public Boolean getRequireLogin() {
        return requireLogin;
    }

    public void setRequireLogin(Boolean requireLogin) {
        this.requireLogin = requireLogin;
    }

    public String getLoginVerificationUrl() {
        return loginVerificationUrl;
    }

    public void setLoginVerificationUrl(String loginVerificationUrl) {
        this.loginVerificationUrl = loginVerificationUrl;
    }

    public String getWebServiceKey() {
        return webServiceKey;
    }

    public void setWebServiceKey(String webServiceKey) {
        this.webServiceKey = webServiceKey;
    }
}