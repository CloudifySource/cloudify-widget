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

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import models.User;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import play.Application;
import play.GlobalSettings;
import play.api.Play;
import play.api.mvc.Results;
import play.api.mvc.SimpleResult;
import play.core.j.JavaResults;
import play.libs.Akka;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import scala.Tuple2;
import scala.collection.JavaConversions;
import server.ApplicationContext;
import server.exceptions.ExceptionResponse;
import server.exceptions.ExceptionResponseDetails;
import server.exceptions.Response401;
import utils.StringUtils;
import utils.Utils;
import akka.util.Duration;
import annotations.AnonymousUsers;
import beans.config.Conf;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SMTPAppender;


/**
 * On system startup trigger event onStart or onStop.
 * 
 * @author Igor Goldenberg
 */
public class Global extends GlobalSettings
{
    private static org.slf4j.Logger logger = LoggerFactory.getLogger( Global.class );

    private Conf conf;
    
	@Override
	public void onStart(Application app)
	{

        loadSpringContext( app );



		// print cloudify configuration
        logger.info("printing configuration");
        conf = ApplicationContext.get().conf();
        logger.info( Json.stringify( Json.toJson( conf ) ) );

        // initialize the server pool.
        // letting the bean do it is incorrect..

        new Thread( new Runnable() { // guy - lets use this for now to fix #33
            @Override
            public void run()
            {
                ApplicationContext.get().getServerPool().init();
            }
        }).start();

        // create Admin user if not exists
		if ( User.find.where().eq("admin", Boolean.TRUE ).findRowCount() <= 0 )
		{
            logger.info( "no admin user. creating from configuration" );
			User adminUser = User.newUser("Cloudify", "Administrator",
                    conf.server.admin.username,
                    conf.server.admin.password );
			adminUser.setAdmin(true);
		    adminUser.save();
		}else{
            logger.info( "found admin user" );
        }

        uploadInitialData( app );

        logger.info("starting destroy server job");
        Akka.system().scheduler().schedule(
                Duration.Zero(),
                Duration.create(1, TimeUnit.MINUTES),
                ApplicationContext.get().getDestroyServersTask()
        );


        try{
            if (!conf.mails.logErrors.isValid() || !conf.smtp.enabled ) {
                logger.info("disabling log errors SMTP appender as smtp configuration or emails configuration is unsatisfactory");
                LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                for (Logger _logger : loggerContext.getLoggerList()) {
                    _logger.detachAppender("EMAIL");
                }
            }else{
                LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

                for (Logger _logger : loggerContext.getLoggerList()) {

                    SMTPAppender smtpAppender = ( SMTPAppender )_logger.getAppender("EMAIL");
                    if ( smtpAppender != null ){
                        smtpAppender.setPassword( conf.smtp.password );
                        smtpAppender.setSTARTTLS( conf.smtp.tls );
                        smtpAppender.setUsername( conf.smtp.user );
                        smtpAppender.setFrom( conf.smtp.user );
                        smtpAppender.setSMTPHost( conf.smtp.host );
                        smtpAppender.setSMTPPort( conf.smtp.port );

                        String subject = smtpAppender.getSubject();
                        smtpAppender.setSubject( conf.application.name + " " + subject );

                        smtpAppender.addTo( conf.mails.logErrors.email );
                        smtpAppender.start();
                        logger.info(_logger.getName() + " has appender " + smtpAppender.getName() + " fully configured");
                        break;
                    }
//                    }else{
//                        logger.info("logger [" + _logger.getName() + "] does not have emails appender");
//                    }
                }
            }
        }catch(Exception e){
            logger.error("unable to reconfigure logback");
        }


        try{
            ApplicationContext.get().getMailSender().sendChangelog();
        }catch(Exception e){
            logger.error("unable to send changelog email",e);
        }
        
        if( conf.settings.isRecoverExecutions ){
        	try{
        		restoreExecutions();
        	}
        	catch( Throwable t ){
        		logger.error( t.toString(), t );
        	}
        }
	}

    private static void restoreExecutions() {

    	if( logger.isDebugEnabled() ){
    		logger.debug( "---restoreExecutions" );
    	}
    	

        ApplicationContext.get().getRestoreExecutionService().restoreExecutions();
	}

	private void uploadInitialData( Application app )
    {
        if ( !conf.settings.initialData.load ) {
            logger.info( "configuration set to not load initial data. skipping initial data" );
            return;
        }

        File file = app.getFile( "conf/initialData/initial-data.json" );

        try {
            ApplicationContext.get().getInitialData().load( FileUtils.readFileToString( file ) );

        } catch ( Exception e ) {
            logger.info( "unable to read initial data from : " + file );
        }
    }

    @Override
    public Result onError( Http.RequestHeader requestHeader, Throwable throwable )
    {

        try {
            if (throwable.getCause() instanceof Response401) {
                return play.mvc.Results.unauthorized(throwable.getCause().getMessage());
            }
        }catch(Exception e){}
        logger.error(  "experienced error [{}]", Utils.requestToString( requestHeader ), throwable );

        // todo : maybe this should be a method implemented in the exception.
        // I assume there is an easier way to do this, but this is what I have so far.
        // the code below simply detects this is our exception, and it sets headers so the GUI can respond accordingly.
        Results.Status status = JavaResults.InternalServerError();
        SimpleResult result;
        if ( throwable.getCause() != null && throwable.getCause() instanceof  ExceptionResponse ){ // customize response according to this exception
            ExceptionResponseDetails res = ( ( ExceptionResponse) throwable.getCause() ).getResponseDetails();
//
//
            Tuple2<String, String> ac = new Tuple2<String, String>( res.getHeaderKey(), res.toJson());
                    ArrayList<Tuple2<String, String>> list = new ArrayList<Tuple2<String, String>>();
                    list.add(ac);
                    scala.collection.immutable.List<Tuple2<String, String>> headers =
                      JavaConversions.asScalaBuffer( list ).toList();
//
//
//            guy -- important.. even though Intellij marks this as error, it is not an error.. ignore it.
            status.header().headers().$plus(  ac );
           result = status.withHeaders( headers );
//            return result;
//            return play.mvc.Results.internalServerError();
        }else{
            return null;
        }

        final SimpleResult finalResult = result;
        return new Result() {
            @Override
            public play.api.mvc.Result getWrappedResult()
            {
                return finalResult;
            }
        };



    }

    @Override
    public Action onRequest( Http.Request request, Method actionMethod )
    {
        if( conf.settings.globalSecurityCheck ){
            if ( !actionMethod.isAnnotationPresent( AnonymousUsers.class ) ){
                // must verify login
                String authToken = request.queryString().get("authToken")[0];
                User.validateAuthToken( authToken );

            }
        }
        return super.onRequest( request, actionMethod );
    }



    @Override
	public void onStop(Application app)
	{
		ApplicationContext.get().getServerBootstrapper().close();
	}

    private ClassPathXmlApplicationContext loadSpringContext( Application app ){
        try{
        GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
        ctx.getEnvironment().setActiveProfiles("standalone");
        ctx.load("*Context.xml");
        ctx.refresh();


        logger.info("loading spring context");
        String contextPath = app.configuration().getString("spring.context");
        String contextProfiles = app.configuration().getString("spring.profiles");

        if (StringUtils.isEmptyOrSpaces(contextProfiles) || StringUtils.isEmptyOrSpaces(contextPath)  ){
            throw new RuntimeException("you need to configure spring.context and spring.profiles");
        }

        logger.info("spring context is at : ["  + contextPath + "] and profiles are [" + contextProfiles + "]" );
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
        applicationContext.getEnvironment().setActiveProfiles(contextProfiles.split(","));
        applicationContext.setConfigLocation(contextPath);
        applicationContext.refresh();
        return applicationContext;
        }catch(RuntimeException e){
            logger.error("unable to load context",e);
            throw e;
        }
    }
}
