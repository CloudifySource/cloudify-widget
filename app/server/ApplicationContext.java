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

import beans.GsMailer;
import beans.HmacImpl;
import beans.config.Conf;
import beans.config.ConfigBean;
import play.modules.spring.Spring;

/**
 * A singleton class that helps to get an instance of different modules and keeps loose decoupling.
 *
 * @author Igor Goldenberg
 */
public class ApplicationContext
{
    private static final String PROC_MANAGER = "procManager";
    private static final String WIDGET_SERVER = "widgetServer";
    private static final String SERVER_POOL = "serverPool";
    private static final String SERVER_BOOTSTRAPPER = "serverBootstrapper";
    private static final String EXPIRED_SERVER_COLLECTOR = "expiredServerCollector";
    private static final String CONF_BEAN = "confBean";
    private static final String MAIL_SENDER = "mailSender";
    private static final String HMAC = "hmac";
    private static final String GS_ROUTES = "gsRoutes";

    public static Conf conf(){
        ConfigBean bean = getBean( CONF_BEAN );
        return bean.getConfiguration();
    }

    public static MailSender getMailSender(){
        return getBean( MAIL_SENDER );
    }

    public static HmacImpl getHmac(){
        return getBean( HMAC );
    }

    public static GsRoutes routes(){
        return getBean( GS_ROUTES );
    }

    public static GsRoutes getGsRoutes(){
        return routes();
    }

    @Deprecated // do not use - use "conf()" instead . do not deleted.
    public static Conf getConf(){
        return conf();
    }

    private static <T> T getBean( String bean ){
        return (T) Spring.getBean( bean );
    }


    public static DeployManager getDeployManager()
	{
        return (DeployManager) Spring.getBean(PROC_MANAGER);
	}

    public static GsMailer.IMailer getMailer(){
        return play.Play.application().plugin( GsMailer.class ).email();
    }

	public static WidgetServer getWidgetServer()
	{
        return (WidgetServer) Spring.getBean(WIDGET_SERVER);
	}

	public static ServerPool getServerPool()
	{
        return (ServerPool) Spring.getBean(SERVER_POOL);
    }

	public static ServerBootstrapper getServerBootstrapper()
	{
		return (ServerBootstrapper) Spring.getBean(SERVER_BOOTSTRAPPER);
	}

	public static ExpiredServersCollector getExpiredServersCollector()
	{
		return (ExpiredServersCollector) Spring.getBean(EXPIRED_SERVER_COLLECTOR);
	}
}
