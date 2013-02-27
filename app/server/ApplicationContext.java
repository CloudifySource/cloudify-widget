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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import mocks.EventMonitorMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.modules.spring.Spring;
import beans.GsMailer;
import beans.HmacImpl;
import beans.config.Conf;

/**
 * A static class that helps to get an instance of different modules and keeps loose decoupling.
 * There are 2 ways to get a bean:
 *
 * 1. On init, set it to a field and serve it on get - suites for singeltons
 * 2. call directly to {@link #getBean(String)} on get - suites for prototypes.
 *
 * If you modify the XML please make sure to modify it here as well.
 *
 * @author Igor Goldenberg
 * @author Guy Mograbi
 *
 *
 */
public class ApplicationContext
{

    private static Logger logger = LoggerFactory.getLogger( ApplicationContext.class );
    @Inject private DeployManager deployManager;
    @Inject private WidgetServer widgetServer;
    @Inject private ServerPool serverPool;
    @Inject private ServerBootstrapper serverBootstrapper;
    @Inject private ExpiredServersCollector expiredServersCollector;
    @Inject private MailSender mailSender;
    @Inject private HmacImpl hmac;
    @Inject private EventMonitor eventMonitor;
    @Inject private Conf conf;

    private static ApplicationContext instance;

    public static ApplicationContext get(){
        if ( instance == null ){ // guy - in case of null, we want to show Spring Exception.. so lets try to reinitialize.
            instance=(ApplicationContext) Spring.getBean("applicationContext");
        }
        return instance;
    }

    @PostConstruct
    public void init(){
        instance = (ApplicationContext) Spring.getBean("applicationContext");
    }


    public  Conf conf(){
        return conf;
    }

    public  MailSender getMailSender(){

        return mailSender;
    }

    public  HmacImpl getHmac(){
        return hmac;
    }


    @Deprecated // do not use - use "conf()" instead . do not deleted.
    public Conf getConf(){
        return conf();
    }

    private  <T> T getBean( String bean ){
        return (T) Spring.getBean( bean );
    }

    public  GsMailer.IMailer getMailer(){
        return play.Play.application().plugin( GsMailer.class ).email();
    }

    public  DeployManager getDeployManager() {
        return deployManager;
    }

    public  WidgetServer getWidgetServer() {
        return widgetServer;
    }

    public  ServerPool getServerPool() {
        return serverPool;
    }

    public  ServerBootstrapper getServerBootstrapper() {
        return serverBootstrapper;
    }

    public  ExpiredServersCollector getExpiredServersCollector() {
        return expiredServersCollector;
    }

    public void setDeployManager( DeployManager deployManager )
    {
        this.deployManager = deployManager;
    }

    public void setWidgetServer( WidgetServer widgetServer )
    {
        this.widgetServer = widgetServer;
    }

    public void setServerPool( ServerPool serverPool )
    {
        this.serverPool = serverPool;
    }

    public void setServerBootstrapper( ServerBootstrapper serverBootstrapper )
    {
        this.serverBootstrapper = serverBootstrapper;
    }

    public void setExpiredServersCollector( ExpiredServersCollector expiredServersCollector )
    {
        this.expiredServersCollector = expiredServersCollector;
    }

    public void setMailSender( MailSender mailSender )
    {
        this.mailSender = mailSender;
    }

    public void setHmac( HmacImpl hmac )
    {
        this.hmac = hmac;
    }

    public void setConf( Conf conf )
    {
        this.conf = conf;
    }

    public EventMonitor getEventMonitor()
    {
        if ( eventMonitor == null ){
            logger.info("eventMonitor is null for some reason");
            return new EventMonitorMock();
        }
        return eventMonitor;
    }

    public void setEventMonitor( EventMonitor eventMonitor )
    {
        this.eventMonitor = eventMonitor;
    }
}
