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
package server;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import beans.scripts.IExecutionRestore;
import cloudify.widget.allclouds.advancedparams.IServerApiFactory;
import cloudify.widget.allclouds.executiondata.ExecutionDataModel;
import cloudify.widget.api.clouds.CloudServerApi;

import cloudify.widget.cli.ICloudifyCliHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import play.Play;
import beans.GsMailer;
import beans.HmacImpl;
import beans.config.Conf;
import beans.tasks.DestroyServersTask;
import bootstrap.InitialData;
import services.IWidgetInstallFinishedSender;
import utils.ResourceManagerFactory;

import java.util.Collection;
import java.util.List;

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
    @Inject private MailSender mailSender;
    @Inject private HmacImpl hmac;
    @Inject private Conf conf;
    @Inject private InitialData initialData;
    @Inject private DestroyServersTask destroyServersTask;
    @Inject private CloudServerApi cloudServerApi;
    @Inject private IExecutionRestore restoreExecutionService;
    @Inject private IServerApiFactory serverApiFactory;
    @Inject private ResourceManagerFactory resourceManagerFactory;
    @Inject private IWidgetInstallFinishedSender widgetInstallFinishedSender;
    @Inject private ICloudifyCliHandler cloudifyCliHandler;

    @Inject private static org.springframework.context.ApplicationContext applicationContext;

    private static ApplicationContext instance;

    public static ApplicationContext get(){
        if ( instance == null ){ // guy - in case of null, we want to show Spring Exception.. so lets try to reinitialize.
            instance= applicationContext.getBeansOfType(ApplicationContext.class).get("applicationContext");
        }
        return instance;
    }

    @Inject
    public void setSpringContext( org.springframework.context.ApplicationContext context ){
        ApplicationContext.applicationContext = context;
    }

    @PostConstruct
    public void init(){
        if ( applicationContext != null ){
            instance = (ApplicationContext) applicationContext.getBean("applicationContext");
        }
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


    private  static <T> T getBean( String bean ){
        return (T) applicationContext.getBean( bean );
    }

    public  GsMailer.IMailer getMailer(){
        GsMailer plugin = Play.application().plugin(GsMailer.class);
        return plugin == null ? new GsMailer.IMailer() {
            @Override
            public void send(GsMailer.GsMailConfiguration mailDetails) {
                logger.info("sending");

            }
        } : plugin.email();
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


    public ICloudifyCliHandler getCloudifyCliHandler() {
        return cloudifyCliHandler;
    }

    public void setCloudifyCliHandler(ICloudifyCliHandler cloudifyCliHandler) {
        this.cloudifyCliHandler = cloudifyCliHandler;
    }

    public InitialData getInitialData()
    {
        return initialData;
    }


    public void setInitialData( InitialData initialData )
    {
        this.initialData = initialData;
    }


    public DestroyServersTask getDestroyServersTask() {
        return destroyServersTask;
    }

    public void setDestroyServersTask(DestroyServersTask destroyServersTask) {
        this.destroyServersTask = destroyServersTask;
    }

    public void setApplicationContext(org.springframework.context.ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public IExecutionRestore getRestoreExecutionService(){
        return restoreExecutionService;
    }

    public CloudServerApi getCloudServerApi() {
        return cloudServerApi;
    }

    public IServerApiFactory getServerApiFactory() {
        return serverApiFactory;
    }

    public void setServerApiFactory(IServerApiFactory serverApiFactory) {
        this.serverApiFactory = serverApiFactory;
    }

    public void setCloudServerApi(CloudServerApi cloudServerApi) {
        this.cloudServerApi = cloudServerApi;
    }

    public ResourceManagerFactory getResourceManagerFactory() {
        return resourceManagerFactory;
    }

    public ExecutionDataModel getNewExecutionDataModel(){ return getBean("executionDataModel"); }

    public void setResourceManagerFactory(ResourceManagerFactory resourceManagerFactory) {
        this.resourceManagerFactory = resourceManagerFactory;
    }

    public IWidgetInstallFinishedSender getWidgetInstallFinishedSender(){
        return widgetInstallFinishedSender;
    }
}


