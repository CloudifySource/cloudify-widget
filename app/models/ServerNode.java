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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.*;

import cloudify.widget.api.clouds.CloudProvider;
import cloudify.widget.api.clouds.CloudServer;
import cloudify.widget.api.clouds.ServerIp;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.db.ebean.Model;
import play.libs.Json;
import utils.CollectionUtils;
import utils.StringUtils;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Junction;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * The ServerNode keeps all metadata of all created and available/busy servers.
 * 
 * @author Igor Goldenberg
 * @see beans.ServerBootstrapperImpl
 */
@Entity
@SuppressWarnings("serial")
@XStreamAlias("server")
public class ServerNode
extends Model
{

    public static final long EXPIRED_TIME = 0L;
    private static Logger logger = LoggerFactory.getLogger(ServerNode.class);

	@Id
	@XStreamOmitField
	private Long id = null;

	@XStreamAsAttribute
	private String serverId;


    @OneToOne
    private Lead lead;


    private Long busySince;

    // indicates when async bootstrap was invoked
    private Date asyncBootstrapStart;


    // indicates when async install was invoked;
    private Date asyncInstallStart;

	@XStreamAsAttribute
	private String publicIP;  // todo : change case to Ip

	@XStreamAsAttribute
	private String privateIP;  // todo : change case to Ip


	@XStreamAsAttribute
    @Lob
	private String privateKey;

	@XStreamAsAttribute
    @Column( name = "api_key") //  can't call a column "key" since it is a keyword
	private String key;

    private String project;

	@XStreamAsAttribute
	private boolean stopped = false;
	
	@XStreamAsAttribute
	private boolean remote = false;


    @OneToOne( cascade = CascadeType.REMOVE, mappedBy="serverNode" )
    WidgetInstance widgetInstance = null;

    @Version
    private long version = 0;
    
    @Lob
    public String advancedParams = null;

    @Lob
    public String recipeProperties = null;


    @OneToOne( cascade = CascadeType.REMOVE)
    public WidgetInstanceUserDetails widgetInstanceUserDetails = null;

    @JsonIgnore
    @OneToMany(mappedBy="serverNode", cascade = CascadeType.REMOVE)
    public List<ServerNodeEvent> events = new LinkedList<ServerNodeEvent>();
    
    @JsonIgnore
    @ManyToOne( )
    private Widget widget;    
	
	public static Finder<Long,ServerNode> find = new Finder<Long,ServerNode>(Long.class, ServerNode.class); 

	public ServerNode( ) {

	} 

	public ServerNode( CloudServer srv )
	{
		this.serverId  = srv.getId();
        ServerIp serverIp = srv.getServerIp();
        publicIP = serverIp.publicIp;
        privateIP = serverIp.privateIp;

	}

	public String getNodeId() // guy - it is dangerous to call this getId as it looks like the getter of "long id"
	{
		return serverId;
	}

    public void setServerId( String serverId )
    {
        this.serverId = serverId;
    }

    public String getPrivateIP()
	{
		return privateIP;
	}

    public Long getId() {
        return id;
    }

    public String getPublicIP()
	{
		return publicIP;
	}
    
    public String getAdvancedParams(){
    	return advancedParams;
    }

    public String getRecipeProperties(){
        return recipeProperties;
    }
    
	public void setAdvancedParams(String text) {
		// TODO Auto-generated method stub
		advancedParams = text;
	}

    public void setRecipeProperties( String text ){ recipeProperties = text; }
	
	public CloudProvider getCloudProvider(){
		String typeStr = Json.parse( advancedParams ).get( "type" ).asText();
		return CloudProvider.findByLabel( typeStr );
	}
    
    public boolean hasAdvancedParams(){
    	return !StringUtils.isEmptyOrSpaces(advancedParams); 
    }    

	public void setPublicIP(String publicIP) {
		this.publicIP = publicIP;
	}

    public Long getTimeLeft() {
        if ( remote ){
            return Long.MAX_VALUE; // leave forever;
        }
        if (widgetInstance != null) {
            if ( lead != null) {
                return busySince + lead.getLeadExtraTimeout() - System.currentTimeMillis();
            } else {
                return Math.max(EXPIRED_TIME, busySince + widgetInstance.getWidget().getLifeExpectancy() - System.currentTimeMillis());
            }

        }
        // server nodes can be busy but without a widget if we just took them from the pool and have yet assigned them a widget instance.
        return null;

    }

    public Long getBusySince() {
        return busySince;
    }

    public void setBusySince(Long busySince) {
        this.busySince = busySince;
    }

    public boolean isExpired()
	{
        Long timeLeft = getTimeLeft();
        return timeLeft != null && timeLeft <= 0;
	}

	public boolean isBusy()
	{
		return busySince != null;
	}

    public void setWidget(Widget widget) {
        this.widget = widget;
    }
    
    public Widget getWidget(){
    	return widget;
    }

	static public int count()
	{
		return find.findRowCount();
	}

	static public List<ServerNode> all()
	{
		return find.all();
	}

    static public List<ServerNode> findByCriteria( QueryConf conf) {
        ExpressionList<ServerNode> where = find.where();
        Junction<ServerNode> disjunction = where.disjunction();

        for (Criteria criteria : conf.criterias) {
            ExpressionList<ServerNode> conjuction = disjunction.conjunction();
            conjuction.eq("1","1"); // solves issues where criteria is actually empty.
            if (criteria.busy != null) {
                if (criteria.busy) {
                    conjuction.isNotNull("busySince");
                } else {
                    conjuction.isNull("busySince");
                }

            }

            if (criteria.remote != null) {
                conjuction.eq("remote", criteria.remote);
            }
            if (criteria.stopped != null) {
                conjuction.eq("stopped", criteria.stopped);
            }

            if ( criteria.serverIdIsNull != null ){
                if ( criteria.serverIdIsNull ){
                    conjuction.isNull("serverId");
                }else{
                    conjuction.isNotNull("serverId");
                }
            }
            
            if ( criteria.widgetIsNull != null ){
            	if ( criteria.widgetIsNull ){
            		conjuction.isNull("widget");
            	}
            	else{
            		conjuction.isNotNull("widget");
            	}
            }

            if ( criteria.asyncBootstrapStartIsNull != null ){
                if ( criteria.asyncBootstrapStartIsNull ){
                    conjuction.isNull("asyncBootstrapStart");
                }else{
                    conjuction.isNotNull("asyncBootstrapStart");
                }
            }

            if ( criteria.asyncInstallStartIsNull != null ){
                if ( criteria.asyncInstallStartIsNull ){
                    conjuction.isNull("asyncInstallStart");
                }else{
                    conjuction.isNotNull("asyncInstallStart");
                }
            }
            
            if ( criteria.widgetInstanceIsNull != null ){
            	if ( criteria.widgetInstanceIsNull ){
            		conjuction.isNull("widgetInstance");
            	}
            	else{
            		conjuction.isNotNull("widgetInstance");
            	}
            }            

            if( criteria.nodeId != null ){
                conjuction.eq("serverId", criteria.nodeId );
            }

            if ( criteria.user != null && !criteria.user.isAdmin()){
                conjuction.eq("user", criteria.user);
            }
        }

        if ( conf.maxRows > 0 ){
            where.setMaxRows( conf.maxRows );
        }

        return where.findList();
    }

	static public ServerNode getServerNode( String serverId )
	{
		return CollectionUtils.first(ServerNode.find.where().eq("serverId", serverId).findList());
	}

    public Date getAsyncBootstrapStart() {
        return asyncBootstrapStart;
    }

    public void setAsyncBootstrapStart(Date asyncBootstrapStart) {
        this.asyncBootstrapStart = asyncBootstrapStart;
    }

    public Date getAsyncInstallStart() {
        return asyncInstallStart;
    }

    public void setAsyncInstallStart(Date asyncInstallStart) {
        this.asyncInstallStart = asyncInstallStart;
    }

    public String toDebugString() {
		return String.format("ServerNode{id='%s', serverId='%s', expirationTime=%d, publicIP='%s', privateIP='%s', busySince=%s}", id, serverId, getTimeLeft(), publicIP, privateIP, busySince);
	}

    @Override
    public String toString()
    {
        return "ServerNode{" +
                "id=" + id +
                ", serverId='" + serverId + '\'' +
                ", expirationTime=" + getTimeLeft() +
                ", publicIP='" + publicIP + '\'' +
                ", privateIP='" + privateIP + '\'' +
                ", busySince=" + busySince +
                ", advancedParams=" + advancedParams +
                ", remote=" + remote +
                ", widget=" + widget +
                ", widgetInstance=" + widgetInstance +
                ", project='" + project + '\'' +
                '}';
    }

    public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(final String privateKey) {
		this.privateKey = privateKey;
	}



	public String getKey() {
		return key;
	}

    private String secretKeyToken(){
        return project + "___" + key;
    }

    public String getSecretKey()
    {
        return (String) Cache.get( secretKeyToken() );
    }

    public void setSecretKey( String secretKey )
    {
        Cache.set( secretKeyToken() , secretKey );
    }

    public void setKey(final String key) {
		this.key = key;
	}

	public String getProject() {
		return project;
	}

	public void setProject(final String project) {
		this.project = project;
	}

	public boolean isStopped() {
		return stopped;
	}

	public void setStopped(final boolean stopped) {
		this.stopped = stopped;
	}

	public boolean isRemote() {
		return remote;
	}

    @Override
    public void delete() {
        logger.info("deleting server node [{}]", remote);
        if( !remote ){
            super.delete();
        }else{
            logger.error("deleting server node ", new RuntimeException());
//            super.delete();
        }
    }

    public void setRemote(boolean remote) {
		this.remote = remote;
	}

    public void setWidgetInstance(WidgetInstance widgetInstance) {
        this.widgetInstance = widgetInstance;
    }

    @JsonIgnore
    public WidgetInstance getWidgetInstance()
    {
        return widgetInstance;
    }
    public ServerNodeEvent errorEvent(String message) {
        return createEvent( message, ServerNodeEvent.Type.ERROR );
    }

    public ServerNodeEvent createEvent( String message, ServerNodeEvent.Type type ){
        logger.info("adding [{}] event [{}]", type, message);
                return new ServerNodeEvent()
                        .setServerNode( this )
                        .setMsg( message )
                        .setEventType(type);
    }

    public ServerNodeEvent infoEvent( String s ) {
        return createEvent(s, ServerNodeEvent.Type.INFO);
    }

    public Lead getLead() {
        return lead;
    }

    public void setLead(Lead lead) {
        this.lead = lead;
    }

    public static ServerNode findByWidgetAndInstanceId(Widget widget, String instanceId) {
        return find.where().eq("widget",widget).eq("id", instanceId).findUnique();
    }

    // guy - todo - formalize this for reuse.
    public static class QueryConf {
        public int maxRows = -1;
        public List<Criteria> criterias = new LinkedList<Criteria>();

        public QueryConf setMaxRows(int maxRows) {
            this.maxRows = maxRows;
            return this;
        }

        public Criteria criteria(){
            Criteria c = new Criteria(this);
            criterias.add(c);
            return c;
        }

    }
    public static class Criteria{
        public Boolean remote = null;
        public Boolean stopped = null;
        public Boolean busy = null;
        public String nodeId = null;
        private QueryConf conf;
        private Boolean serverIdIsNull;
        private Boolean widgetIsNull;
        private Boolean widgetInstanceIsNull;
        private Boolean asyncBootstrapStartIsNull;
        private Boolean asyncInstallStartIsNull;
        private User user;

        public Criteria(QueryConf conf) {
            this.conf = conf;
        }

        public Criteria setRemote(Boolean remote) {
            this.remote = remote;
            return this;
        }

        public QueryConf done(){
            return conf;
        }

        public Criteria setStopped(Boolean stopped) {
            this.stopped = stopped;
            return this;
        }

        public Criteria setBusy(Boolean busy) {
            this.busy = busy;
            return this;
        }

        public Criteria setAsyncBootstrapStartIsNull(Boolean asyncBootstrapStartIsNull) {
            this.asyncBootstrapStartIsNull = asyncBootstrapStartIsNull;
            return this;
        }

        public Criteria setAsyncInstallStartIsNull(Boolean asyncInstallStartIsNull) {
            this.asyncInstallStartIsNull = asyncInstallStartIsNull;
            return this;
        }

        public Criteria setNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }


        public Criteria setServerIdIsNull(boolean serverIdIsNull) {
            this.serverIdIsNull = serverIdIsNull;
            return this;
        }
        
        public Criteria setWidgetIsNull(boolean widgetIsNull) {
            this.widgetIsNull = widgetIsNull;
            return this;
        }
        
        public Criteria setWidgetInstanceIsNull(boolean widgetInstanceIsNull) {
            this.widgetInstanceIsNull = widgetInstanceIsNull;
            return this;
        }        

        public Criteria setUser(User user) {
            this.user = user;
            return this;
        }


    }


}