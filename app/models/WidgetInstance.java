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

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;
import utils.Utils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import controllers.Application;

/**
 * This class represents a widget instance with a deployment metadata and instantiated on {@link Application#start(String, String, String)} }
 * The metadata contains on which server the widget instance has been deployed.
 * 
 * @author Igor Goldenberg
 * @see Application
 */
@Entity
@XStreamAlias("instance")
public class WidgetInstance
	extends Model
{
	private static final long serialVersionUID = 1L;

	@Id
	@XStreamOmitField
	private Long id;
	
	@XStreamAsAttribute
	private String instanceId;
	
	@XStreamAsAttribute
	private Boolean anonymouse = false;
	
	@XStreamAsAttribute
	private String publicIP;  // todo : change case to Ip
	
	@XStreamAlias("link")
	private ConsoleLink link;
	
	public static Finder<Long,WidgetInstance> find = new Finder<Long,WidgetInstance>(Long.class, WidgetInstance.class); 

	private static final String HOST_TOKEN = "$HOST";

	final static private class ConsoleLink
	{
		String title;
		String url;
		
		ConsoleLink( String title, String url )
		{
			this.title = title;
			this.url = url;
		}
	}
	
	public WidgetInstance( String instanceId, String publicIP )
	{
		this( instanceId, publicIP, null, null );
	}
	
	public WidgetInstance( String instanceId, String publicIP, String consoleName, String consoleUrl )
	{
		this.instanceId = instanceId;
		this.publicIP = publicIP;
		
		String consoleLink = "http://" + publicIP;
		if ( consoleUrl != null & consoleUrl.indexOf(HOST_TOKEN) != -1 )
			consoleLink = consoleUrl.replace(HOST_TOKEN, publicIP);
		
		link = new ConsoleLink(consoleName, consoleLink);
	}
	
	public boolean isAnonymouse()
	{
		return anonymouse;
	}

	public void setAnonymouse(boolean anonymouse)
	{
		this.anonymouse = anonymouse;
	}

	public String getPublicIP()
	{
		return publicIP;
	}

	public void setPublicIP(String publicIP)
	{
		this.publicIP = publicIP;
	}
	
	public String getInstanceId()
	{
		return instanceId;
	}

	public void setInstanceId(String instanceId)
	{
		this.instanceId = instanceId;
	}
	
	public static void deleteByInstanceId( String instanceId )
	{
		WidgetInstance widgetInstance = find.where().eq("instanceId", instanceId).findUnique();
		if ( widgetInstance != null )
			 widgetInstance.delete();
	}

	@Override
	public String toString()
	{
		return Utils.reflectedToString(this);
	}
}