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

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.Play;

/**
 * Cloudify configuration class. All config properties locates in /conf/cloudify.conf file.
 *
 * @author Igor Goldenberg
 */
public class Config
{

    private static Logger logger = LoggerFactory.getLogger( Config.class );
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
	final public static long CLOUDIFY_DEPLOY_TIMEOUT; // ms

    private static <T> T getValue( T value, T defaultValue ){
          return value == null ? defaultValue : value;
    }

    private static String getFullPath( String relativePath )
    {
        File file = Play.application().getFile( relativePath );
        if ( !file.exists() ) {
            logger.warn( "file %s does not exists but required by the configuration", file.getAbsolutePath() );
        }
        return file.getAbsolutePath();
    }

	static
	{
		try
		{
            logger.info( "loading configuration" );
            // guy - using play conf instead of a properties files.
            // this way we get the benefit of using all of play's amazing configuration features.
            Configuration conf = Play.application().configuration();


			WIDGET_SERVER_ID                = getValue( conf.getString("widget.server-id") , "466999");
			WIDGET_STOP_TIMEOUT             = getValue( conf.getInt( "widget.stop-timeout-sec" ), 30 );

			// server pool properties
			SERVER_POOL_COLD_INIT           = getValue( conf.getBoolean( "server-pool.cold-init" ), false ) ;
			SERVER_POOL_MIN_NODES           = getValue( conf.getInt("server-pool.min-nodes"), 2 );
			SERVER_POOL_MAX_NODES           = getValue( conf.getInt("server-pool.max-nodes" ), 5 );

            // guy - consider using time expression (60m) or (60000ms) so it will be clear
			SERVER_POOL_EXPIRATION_TIME     = TimeUnit.MINUTES.toMillis( getValue( conf.getInt( "server-pool.expiration-time-min" ), 60 ) );

			// bootstrap properties
			COMPUTE_SERVER_NAME_PREF        = getValue( conf.getString("server.bootstrap.servername-prefix"), "cloudify_pool_server" );
			COMPUTE_ZONE_NAME               = getValue( conf.getString( "server.bootstrap.zone-name" ), "az-1.region-a.geo-1" );
			COMPUTE_KEY_PAIR                = getValue( conf.getString("server.bootstrap.key-pair" ), "cloudify" );
			COMPUTE_SECURITY_GROUP          = getValue( conf.getString("server.bootstrap.security-group" ), "default" );
			COMPUTE_FLAVOR_ID               = getValue( conf.getString("server.bootstrap.flavor-id"), "102" );
			COMPUTE_IMAGE_ID                = getValue( conf.getString("server.bootstrap.image-id"), "1358" );
			COMPUTE_SSH_USER                = getValue( conf.getString("server.bootstrap.ssh-user"), "root" );
			COMPUTE_SSH_PORT                = getValue( conf.getInt( "server.bootstrap.ssh-port" ), 22 );
			COMPUTE_SSH_PRIVATE_KEY         = getFullPath( getValue( conf.getString( "server.bootstrap.ssh-private-key" ), "/bin/hpcloud.pem" ) );
			COMPUTE_APIKEY                  = getValue( conf.getString( "server.bootstrap.api-key"), "" );
			COMPUTE_USERNAME                = getValue( conf.getString( "server.bootstrap.username"), "" );
			COMPUTE_PROVIDER                = getValue( conf.getString( "server.bootstrap.cloud-provider" ), "hpcloud-compute" );
			COMPUTE_BOOTSTRAP_SCRIPT        =  "file:" + getFullPath( getValue( conf.getString("server.bootstrap.script" ), "/bin/bootstrap_machine.sh" ) );

			CLOUDIFY_DEPLOY_SCRIPT          = getFullPath( getValue( conf.getString( "cloudify.deploy-script" ), "/bin/deployer.sh" ) );

            // guy - same here, consider using time expression for better clarity
			CLOUDIFY_DEPLOY_TIMEOUT         = TimeUnit.MINUTES.toMillis( getValue( conf.getInt( "cloudify.deploy.watchdog-process-timeout-min" ), 2 ) );

			ADMIN_USERNAME                  = getValue( conf.getString( "server.admin.username" ), "admin@gigaspaces.com");
			ADMIN_PASSWORD                  = getValue( conf.getString( "server.admin.password" ), "cloudify1324");

			MESSAGES_CONFIG_FILE            = getFullPath( getValue( conf.getString( "cloudify.messages-config-file" ), "/conf/messages.conf" ) );
		} catch ( Exception e ){
            throw new ServerException( "unable to load configuration", e  );
        }
	}


	public static String print()
	{
		return new Config().toString();
	}


    // TODO : lets reuse Utils function
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
                b.append( f.getName() ).append( "=" ).append( f.get( this ) ).append( "\n" );
			} catch ( Exception e)
			{
				// pass, don't print
			}
		}

		return b.toString();
	}
}
