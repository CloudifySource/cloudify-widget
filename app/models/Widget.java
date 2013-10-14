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
package models;

import java.io.File;
import java.util.*;

import javax.persistence.*;

import beans.Recipe;
import beans.config.ServerConfig;
import org.apache.commons.collections.Predicate;
import org.codehaus.jackson.annotate.JsonBackReference;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import org.codehaus.jackson.map.annotate.JsonRootName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.i18n.Messages;
import server.ApplicationContext;
import server.exceptions.ServerException;
import utils.CollectionUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import controllers.WidgetAdmin;
import utils.StringUtils;

/**
 * This class represents a widget metadata and relates to a specific {@link User}.
 * See {@link WidgetAdmin#createNewWidget}
 * 
 * @author Igor Goldenberg
 * @see WidgetAdmin
 */
@Entity
@XStreamAlias("widget")
@SuppressWarnings("serial")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Widget
	extends Model
{
	@Id
	private Long id;

    @Constraints.Required
    private String productName;
    @Constraints.Required
    private String providerURL;
    @Constraints.Required
    private String productVersion;
    @Constraints.Required
    private String title;
	private String youtubeVideoUrl;
    @Constraints.Required
	private String recipeURL;
	private Boolean allowAnonymous;
    @Constraints.Required
	private String apiKey;

    @JsonIgnore
	private Integer launches;
	private Boolean enabled;

    @JsonProperty(value="consolename")
	private String consoleName;


    // guy - this is a temporary work around until cloudify will sort
    // https://cloudifysource.atlassian.net/browse/CLOUDIFY-1258
    private String recipeName;


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private WidgetIcon icon;

    @JsonProperty(value="consoleurl")
	private String consoleURL;

    @JsonProperty( value="rootpath")
    private String recipeRootPath;
    private Boolean requireLogin = false; // should this widget support login?
    private String loginVerificationUrl = null; // url to verify user IDs.
    private String webServiceKey=null; // secret key we add on the web service calls.



    private long lifeExpectancy = 0;
    @JsonIgnore
    @ManyToOne( optional = false )
    private User user;

    @Lob
    private String description;

    // for remote bootstrap, use this service name to construct the console link.
    //
    private String consoleUrlService;


    @JsonIgnore
	@OneToMany(cascade=CascadeType.ALL, mappedBy = "widget")
	private List<WidgetInstance> instances = new LinkedList<WidgetInstance>(  );

	public static Finder<Long,Widget> find = new Finder<Long,Widget>(Long.class, Widget.class);

    private static Logger logger = LoggerFactory.getLogger( Widget.class );



    @XStreamAlias("status")
    @JsonRootName("status")
    final static public class Status {

        /**
         * This class serves as status of the widget instance
         */
        public final static String STATE_RUNNING = "running";
        public final static String STATE_STOPPED = "stopped";

        private State state = State.RUNNING;

        private List<String> output;
        private List<String> rawOutput; // for debug purposes
        private Integer timeleft; // minutes
        private Long timeleftMillis; // millis
        private String publicIp;
        private String instanceId;  // server node instance id - NOT widget instance id.
        private Boolean remote;
        private Boolean hasPemFile;
        private WidgetInstance.ConsoleLink consoleLink;
        private String message; // for errors
        private Boolean instanceIsAvailable; // if install finished
        private Boolean cloudifyUiIsAvailable;



        public static enum State {
            STOPPED, RUNNING;
        }
        public Status() {
        }

        public void setCloudifyUiIsAvailable(Boolean cloudifyUiIsAvailable) {
            this.cloudifyUiIsAvailable = cloudifyUiIsAvailable;
        }

        public Boolean getInstanceIsAvailable() {
            return instanceIsAvailable;
        }

        public Boolean getCloudifyUiIsAvailable() {
            return cloudifyUiIsAvailable;
        }

        public void setInstanceIsAvailable(Boolean instanceIsAvailable) {
            this.instanceIsAvailable = instanceIsAvailable;
        }


        public void setConsoleLink(WidgetInstance.ConsoleLink link) {
            this.consoleLink = link;
        }

        public void setState(State state) {
            this.state = state;
        }

        public Long getTimeleftMillis() {
            return timeleftMillis;
        }

        public void setTimeleftMillis(Long timeleftMillis) {
            this.timeleftMillis = timeleftMillis;
        }

        public void setTimeleft(Integer timeleft) {
            this.timeleft = timeleft == null ? 0 : timeleft;
        }

        public Status setHasPemFile(Boolean hasPemFile) {
            this.hasPemFile = hasPemFile;
            return this;
        }

        public Status setRemote(Boolean remote) {
            this.remote = remote;
            return this;
        }

        public void setPublicIp(String publicIp) {
            this.publicIp = publicIp;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public Boolean getRemote() {
            return remote;
        }

        public Boolean getHasPemFile() {
            return hasPemFile;
        }

        public Status setInstanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public void setOutput(List<String> output) {
            this.output = output;
        }

        public List<String> getRawOutput()
        {
            return rawOutput;
        }

        public void setRawOutput( List<String> rawOutput )
        {
            this.rawOutput = rawOutput;
        }

        public List<String> getOutput() {
            if (CollectionUtils.isEmpty(output)) {
                output = new LinkedList<String>();
                output.add(Messages.get("wait.while.preparing.env"));
            }
            return output;
        }

        public State getState() {
            return state;
        }

        public Integer getTimeleft() {
            return timeleft;
        }

        public String getPublicIp() {
            return publicIp;
        }

        public String getInstanceId() {
            return instanceId;
        }

        public WidgetInstance.ConsoleLink getConsoleLink() {
            return consoleLink;
        }
    }


    // for serialization
    public Widget(){

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
        this.recipeRootPath = recipeRootPath;
        init();
	}

    public void init(){
        this.enabled = true;
        this.launches = 0;
        this.apiKey = UUID.randomUUID().toString();
    }
	
	public WidgetInstance addWidgetInstance( ServerNode serverNode, File recipeDir )
	{
        Recipe.Type recipeType = new Recipe(recipeDir).getRecipeType();
        WidgetInstance wInstance = new WidgetInstance();
        wInstance.setRecipeType( recipeType );
        wInstance.setServerNode( serverNode );
        wInstance.setInstallName( toInstallName() );
		if (instances == null){
			instances = new ArrayList<WidgetInstance>();
        }
		instances.add( wInstance );
		save();
        // server node has the foreign key..
        serverNode.setWidgetInstance(wInstance);
        serverNode.save();

		return wInstance;
	}

    public String toInstallName(){

        if ( ApplicationContext.get().conf().features.autoGeneratedRecipeName.on ){
            return ("" + productName + productVersion).toLowerCase().replaceAll( "[^a-z0-9]", "_" );
        }
        else if ( recipeName == null ){
            throw new RuntimeException( String.format("invalid state. all widgets should have a recipe name. recipe id [%s] does not have one", id) );
        }
        return recipeName;
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

    public String getConsoleName() {
        return consoleName;
    }

    public String getConsoleURL() {
        return consoleURL;
    }

    public static List<Widget> findByUser(User user) {
        return find.where().eq("user", user ).findList();
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
	
	public Widget regenerateApiKey( )
	{
		apiKey = UUID.randomUUID().toString();
		save();
        refresh();
        return this;
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

    @JsonProperty("instances")
    @Transient
    public List<WidgetInstance> getViableInstances(){
        if ( CollectionUtils.isEmpty( instances )){
            return new LinkedList<WidgetInstance>();
        }
        List<WidgetInstance> result = new LinkedList<WidgetInstance>( instances );
        CollectionUtils.filter( result, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return !((WidgetInstance) o).isCorrupted();
            }
        });
        return result;
    }


	public List<WidgetInstance> getInstances()
	{
		return instances;
	}

    @JsonIgnore
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

    @JsonIgnore
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

    @JsonIgnore
    public String getYoutubeVideoKey(){
        try{
        if ( StringUtils.isEmpty( youtubeVideoUrl )){
            return null;
        }else if ( StringUtils.contains( youtubeVideoUrl, "/embed/" ) ){

            return youtubeVideoUrl.split( "/embed/" )[1];

        }else{
            logger.error( "unable to get youtube key from [{}]", youtubeVideoUrl );
            return null;
        }
        }catch(Exception e){
            logger.error( "error while getting youtube key from [{}]", youtubeVideoUrl );
            return null;
        }
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

    @JsonIgnore
	public void setLaunches(Integer launches)
	{
		this.launches = launches;
	}
	
	public Boolean isEnabled()
	{
		return enabled;
	}

	public Widget setEnabled( Boolean enabled )
	{
		this.enabled = enabled;
        return this;
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
    public String toString() {
        return String.format("Widget{id=%d, title='%s', apiKey='%s', launches=%d, enabled=%s, recipeRootPath='%s'}", id, title, apiKey, launches, enabled, recipeRootPath);
    }

    public long getLifeExpectancy() {
        // by default use configuration
        return lifeExpectancy == 0 ? ApplicationContext.get().conf().server.pool.expirationTimeMillis : lifeExpectancy ;
    }

    public void setLifeExpectancy(long lifeExpectancy) {
        ServerConfig.PoolConfiguration poolConf = ApplicationContext.get().conf().server.pool;
        lifeExpectancy = Math.max( lifeExpectancy, poolConf.minExpiryTimeMillis );
        lifeExpectancy = Math.min(lifeExpectancy, poolConf.maxExpirationTimeMillis);
        this.lifeExpectancy = lifeExpectancy;
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
        return user == null ? "null" : user.getEmail();
    }

    @JsonIgnore
    public void setUsername( String username ){

    }

    @JsonIgnore
    public void setNumOfInstances( int i){

    }


    @Transient
    @JsonProperty
    public int getNumOfInstances(){
        return instances.size();
    }

    public void setRequiresLogin( boolean requires ){
        requireLogin = requires;
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

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    @JsonIgnore
    public void setHasIcon( boolean hasIcon ){

    }

    public boolean isHasIcon(){
        return icon != null; // todo: verify this does not load "WidgetIcon" lazily.
    }

    public void setIcon( WidgetIcon icon ){
        this.icon = icon;
    }

    public String getConsoleUrlService()
    {
        return consoleUrlService;
    }

    public void setConsoleUrlService( String consoleUrlService )
    {
        this.consoleUrlService = consoleUrlService;
    }

    public String getRecipeName()
    {
        return recipeName;
    }

    public void setRecipeName( String recipeName )
    {
        this.recipeName = recipeName;
    }
}