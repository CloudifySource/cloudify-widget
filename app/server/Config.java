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
package server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import play.Play;

/**
 * Cloudify configuration class. All config properties locates in /conf/cloudify.conf file.
 * 
 * @author Igor Goldenberg
 */
public class Config
{
	
	final public static String WIDGET_SERVER_ID;
	final public static int WIDGET_STOP_TIMEOUT; // sec
	
	final public static boolean SERVER_POOL_COLD_INIT;
	final public static int SERVER_POOL_MIN_NODES;
	final public static int SERVER_POOL_MAX_NODES;
	final public static long SERVER_POOL_EXPIRATION_TIME; // ms
	
	final public static String COMPUTE_SERVER_NAME_PREF;
	final public static String COMPUTE_ZONE_NAME;
	final public static String COMPUTE_KEY_PAIR;
	final public static String COMPUTE_SECURITY_GROUP;
	final public static String COMPUTE_FLAVOR_ID;
	final public static String COMPUTE_IMAGE_ID;
	final public static String COMPUTE_SSH_USER;		
	final public static int    COMPUTE_SSH_PORT;		
	final public static String COMPUTE_SSH_PRIVATE_KEY;
	final public static String COMPUTE_APIKEY;	
	final public static String COMPUTE_USERNAME;	
	final public static String COMPUTE_PROVIDER;
	final public static String COMPUTE_BOOTSTRAP_SCRIPT;
	
	final public static String CLOUDIFY_DEPLOY_SCRIPT;
	
	final public static String ADMIN_USERNAME;
	final public static String ADMIN_PASSWORD;
	
	final public static String MESSAGES_CONFIG_FILE;
	
	// if the process hasn't finished within the defined timeout, the process will be killed
	final static long CLOUDIFY_DEPLOY_TIMEOUT; // ms

	
	static
	{
		try
		{
			Properties props = new Properties();
			props.load(new FileInputStream(Play.application().path() + "/conf/cloudify.conf"));
			
			WIDGET_SERVER_ID = props.getProperty("widget.server-id", "466999");
			WIDGET_STOP_TIMEOUT = Integer.parseInt( props.getProperty("widget.stop-timeout-sec", "30"));
			
			// server pool properties
			SERVER_POOL_COLD_INIT = Boolean.valueOf(props.getProperty("server-pool.cold-init", "false"));
			SERVER_POOL_MIN_NODES = Integer.parseInt( props.getProperty("server-pool.min-nodes", "2") );
			SERVER_POOL_MAX_NODES = Integer.parseInt( props.getProperty("server-pool.max-nodes", "5") );
			SERVER_POOL_EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(Integer.parseInt( props.getProperty("server-pool.expiration-time-min", "60") ));
			
			// bootstrap properties
			COMPUTE_SERVER_NAME_PREF = props.getProperty("server.bootstrap.servername-prefix", "cloudify_pool_server");
			COMPUTE_ZONE_NAME = props.getProperty("server.bootstrap.zone-name", "az-1.region-a.geo-1");
			COMPUTE_KEY_PAIR = props.getProperty("server.bootstrap.key-pair", "cloudify");
			COMPUTE_SECURITY_GROUP = props.getProperty("server.bootstrap.security-group", "default");
			COMPUTE_FLAVOR_ID = props.getProperty("server.bootstrap.flavor-id", "102");
			COMPUTE_IMAGE_ID = props.getProperty("server.bootstrap.image-id", "1358");
			COMPUTE_SSH_USER = props.getProperty("server.bootstrap.ssh-user", "root");
			COMPUTE_SSH_PORT = Integer.parseInt(props.getProperty("server.bootstrap.ssh-port", "22"));
			COMPUTE_SSH_PRIVATE_KEY = Play.application().path()  + props.getProperty("server.bootstrap.ssh-private-key", "/bin/hpcloud.pem");
			COMPUTE_APIKEY = props.getProperty("server.bootstrap.api-key", "");
			COMPUTE_USERNAME = props.getProperty("server.bootstrap.username", "");
			COMPUTE_PROVIDER = props.getProperty("server.bootstrap.cloud-provider", "hpcloud-compute");
			COMPUTE_BOOTSTRAP_SCRIPT =  "file:" + Play.application().path() + props.getProperty("server.bootstrap.script", "/bin/bootstrap_machine.sh");	
		
			CLOUDIFY_DEPLOY_SCRIPT = Play.application().path() + props.getProperty("cloudify.deploy-script", "/bin/deployer.sh"); 
			CLOUDIFY_DEPLOY_TIMEOUT = TimeUnit.MINUTES.toMillis(Integer.parseInt(props.getProperty("cloudify.deploy.watchdog-process-timeout-min", "2")));
		
			ADMIN_USERNAME = props.getProperty("server.admin.username", "admin@gigaspaces.com");
			ADMIN_PASSWORD = props.getProperty("server.admin.password", "cloudify1324");
			
			MESSAGES_CONFIG_FILE = Play.application().path() + props.getProperty("cloudify.messages-config-file", "/conf/messages.conf"); 
		} catch (FileNotFoundException e)
		{
			throw new ServerException(e.getMessage());
		} catch (IOException e)
		{
			throw new ServerException("Failed to load config file", e);
		}
	}
	
	
	public static String print()
	{
		return new Config().toString();
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder("Cloudify widget configuration:\n");

		Field[] fields = getClass().getDeclaredFields();
		Field.setAccessible(fields, true);

		for (Field f : fields)
		{
			try
			{
				b.append(f.getName() + "=" + f.get(this) + "\n");
			} catch (IllegalAccessException e)
			{
				// pass, don't print
			}
		}

		return b.toString();
	}
}
