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

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Junction;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.ebean.Model;
import utils.CollectionUtils;
import utils.Utils;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private static Logger logger = LoggerFactory.getLogger(ServerNode.class);

	@Id
	@XStreamOmitField
	private Long id;

	@XStreamAsAttribute
	private String serverId;

	@XStreamAsAttribute
	private Long expirationTime;

	@XStreamAsAttribute
	private String publicIP;  // todo : change case to Ip

	@XStreamAsAttribute
	private String privateIP;  // todo : change case to Ip

	@XStreamAsAttribute
	private Boolean busy = false;

	@XStreamAsAttribute
    @Lob
	private String privateKey;

	@XStreamAsAttribute
    @Column( name = "api_key") //  can't call a column "key" since it is a keyword
	private String key;

    private String project;

	@XStreamAsAttribute
    @Column( name="api_secret_key") // keep convention from key
	private String secretKey;

	@XStreamAsAttribute
	private boolean stopped = false;
	
	@XStreamAsAttribute
	private boolean remote = false;

    @OneToOne( cascade = CascadeType.REMOVE )
    WidgetInstance widgetInstance = null;

    @Version
    private long version = 0;

    @OneToMany(mappedBy="serverNode", cascade = CascadeType.REMOVE)
    public List<ServerNodeEvent> events = new LinkedList<ServerNodeEvent>();
	
	public static Finder<Long,ServerNode> find = new Finder<Long,ServerNode>(Long.class, ServerNode.class); 

	public ServerNode( ) {

	} 

	public ServerNode( Server srv )
	{
		this.serverId  = srv.getId();
        Utils.ServerIp serverIp = Utils.getServerIp( srv );
        publicIP = serverIp.publicIp;
        privateIP = serverIp.privateIp;
        this.expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis( 30 ); // default unless configured otherwise
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

	public void setPublicIP(String publicIP) {
		this.publicIP = publicIP;
	}

	public Long getExpirationTime()
	{
		return expirationTime;
	}

	public void setExpirationTime(Long expirationTime)
	{
		this.expirationTime = expirationTime;
	}

	public long getElapsedTime()
	{
        return Math.max(  expirationTime - System.currentTimeMillis(), 0 );
	}

	public boolean isExpired()
	{
		return getElapsedTime() < 0;
	}

	public boolean isBusy()
	{
		return busy;
	}

	public void setBusy( boolean isBusy )
	{
		this.busy = isBusy;
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
            if (criteria.busy != null) {
                conjuction.eq("busy", criteria.busy);
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

	public String toDebugString() {
		return String.format("ServerNode{id='%s', serverId='%s', expirationTime=%d, publicIP='%s', privateIP='%s', busy=%s}", id, serverId, expirationTime, publicIP, privateIP, busy);
	}

    @Override
    public String toString()
    {
        return "ServerNode{" +
                "id=" + id +
                ", serverId='" + serverId + '\'' +
                ", expirationTime=" + expirationTime +
                ", publicIP='" + publicIP + '\'' +
                ", privateIP='" + privateIP + '\'' +
                ", busy=" + busy +
                ", remote=" + remote +
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

    public String getSecretKey()
    {
        return secretKey;
    }

    public void setSecretKey( String secretKey )
    {
        this.secretKey = secretKey;
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
                        .setEventType( type );
    }

    public ServerNodeEvent infoEvent( String s ) {
        return createEvent( s, ServerNodeEvent.Type.INFO );
    }


    // guy - todo - formalize this for reuse.
    public static class QueryConf {
        public int maxRows;
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

        public Criteria setNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }


        public Criteria setServerIdIsNull(boolean serverIdIsNull) {
            this.serverIdIsNull = serverIdIsNull;
            return this;
        }
    }
}