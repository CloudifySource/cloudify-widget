/*
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
 */

package beans;

import beans.config.Conf;
import beans.config.SmtpConf;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import play.Application;
import play.api.Plugin;
import play.libs.F;
import server.ApplicationContext;

import java.util.Collection;
import java.util.LinkedList;

/**
 * This is a GigaSpaces mailer plugin implementation for play 2.0.
 * It is based on the typesage mailer plugin and enhances it to suite our needs.
 *
 * In order to use it properly, you should add the following row to "play.plugins" file
 * 1500:beans.GsMailerPluginImpl
 *
 */
public class GsMailer implements Plugin {


    private static Logger logger = LoggerFactory.getLogger( GsMailer.class );

    private IMailer mockMailer;
    private IMailer gsMailer;
    private Application app;

    public GsMailer( Application application )
    {
        this.app = application;
    }

    @Override
    public void onStart()
    {
        try {

            logger.info( "starting... " );
            mockMailer = new GsMockMailer();
            logger.info( "enabled {}", enabled() );
        } catch ( RuntimeException e ) {
            System.out.println( "e = " + e );
        }
    }

    @Override
    public void onStop()
    {
        logger.info( "stopping" );
    }

    @Override
    public boolean enabled()
    {
        logger.info( "checking if enabled..." );
        return app.configuration().getBoolean( "smtp.enabled" ) != Boolean.FALSE;
    }

    private IMailer getGsMailer(){
        if ( gsMailer == null ){
            gsMailer = new GsMailerImpl( ApplicationContext.get().conf().smtp );
        }
        return gsMailer;
    }

    private boolean isMock(){
        return ApplicationContext.get().conf().smtp.mock;
    }

    public IMailer email()
    {
        return isMock() ? mockMailer : getGsMailer();
    }


    public static class GsMailConfiguration{
        public String charset = "utf-8";
        public String subject = "";
        public String bodyText = "";
        public String bodyHtml = "";
        public Mailer from;
        public Mailer replyTo;
        public MailerCollection to = new MailerCollection(  );
        public MailerCollection cc = new MailerCollection(  );
        public MailerCollection bcc = new MailerCollection(  );
        public HeaderCollection headers = new HeaderCollection();

        public GsMailConfiguration setCharset( String charset )
        {
            this.charset = charset;
            return this;
        }

        public GsMailConfiguration setSubject( String subject )
        {
            this.subject = subject;
            return this;
        }

        public GsMailConfiguration setBodyText( String bodyText )
        {
            this.bodyText = bodyText;
            return this;
        }

        public GsMailConfiguration setBodyHtml( String bodyHtml )
        {
            this.bodyHtml = bodyHtml;
            return this;
        }

        public GsMailConfiguration setFrom( String email, String name  )
        {
            this.from = new Mailer().setEmail( email ).setName( name );
            return this;
        }

        public GsMailConfiguration setReplyTo( Mailer replyTo )
        {
            this.replyTo = replyTo;
            return this;
        }

        public GsMailConfiguration addHeader( String name, String value ){
            headers.add( new Header().setName( name ).setValue( value ) );
            return this;
        }

        public GsMailConfiguration addRecipient( RecipientType type, String email, String name){
            Mailer mailer = new Mailer().setName( name ).setEmail( email );
            switch ( type ) {

                case TO:
                    to.add( mailer );
                    break;
                case CC:
                    cc.add( mailer );
                    break;
                case BCC:
                    bcc.add( mailer );
                    break;
            }
            return this;
        }

        @Override
        public String toString()
        {
            return "GsMailConfiguration{" +
                    "charset='" + charset + '\'' +
                    ", subject='" + subject + '\'' +
                    ", bodyText='" + bodyText + '\'' +
                    ", bodyHtml='" + bodyHtml + '\'' +
                    ", from=" + from +
                    ", replyTo=" + replyTo +
                    ", to=" + to +
                    ", cc=" + cc +
                    ", bcc=" + bcc +
                    ", headers=" + headers +
                    '}';
        }
    }


    public static class Header{
        public String name;
        public String value;

        public Header setName( String name )
        {
            this.name = name;
            return this;
        }

        public Header setValue( String value )
        {
            this.value = value;
            return this;
        }
    }


    public static abstract class CustomCollection<T>{
        Collection<T> innerCollection = new LinkedList<T>(  );

        public void foreach( F.Function<T, ?> function ){
            if ( !CollectionUtils.isEmpty( innerCollection )){
                for ( T t : innerCollection ) {
                    try{
                        function.apply( t );
                    }catch(Throwable e){
                        throw new  RuntimeException( "error while invoking action",e );
                    }
                }
            }
        }

        public void add( T ... t )
        {
            add( CollectionUtils.arrayToList( t ) );
        }

        public void add( Collection<T> t )
        {
            this.innerCollection.addAll( t );
        }

        @Override
        public String toString()
        {
            return innerCollection.toString();
        }
    }
    public static class HeaderCollection extends  CustomCollection<Header>{

    }
    public static class Mailer{
        public String email;
        public String name;

        public Mailer setEmail( String email )
        {
            this.email = email;
            return this;
        }

        public Mailer setName( String name )
        {
            this.name = name;
            return this;
        }

        @Override
        public String toString()
        {
            return "Mailer{" +
                    "email='" + email + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    private static class MailerCollection extends CustomCollection<Mailer>{ }

    private  static abstract class MailerAction{
        abstract public void apply( Mailer mailer);
    }

    public static enum RecipientType{
        TO, CC, BCC
    }

    private static class AddMailerAction implements F.Function<Mailer, Void> {
        private RecipientType type;
        private Email email;

        private AddMailerAction( RecipientType type, Email email )
        {
            this.type = type;
            this.email = email;
        }

        @Override
        public Void apply( Mailer mailer )
        {
            try{
            switch ( type ) {

                case TO:
                    email.addTo( mailer.email, mailer.name);
                    break;
                case CC:
                    email.addCc( mailer.email, mailer.name );
                    break;
                case BCC:
                    email.addBcc( mailer.email, mailer.name );
                    break;
            }
            }catch(Exception e){
                logger.error( String.format( "unable to apply action on mailer {}", mailer ),e );
            }
            return null;
        }
    }

    public static interface IMailer{
        public void send( GsMailConfiguration mailDetails );
    }

    public static class GsMailerImpl implements IMailer{

        SmtpConf smtpConf;

        public GsMailerImpl( SmtpConf conf )
        {
            this.smtpConf = conf;
//            super( conf.getHost(), conf.getPort(), conf.isSsl(), new Some( conf.getUser() ), new Some( conf.getPassword() ) );
        }

        public void send( GsMailConfiguration mailDetails )
        {
            try {
                final MultiPartEmail email = createEmailer( mailDetails.bodyText, mailDetails.bodyHtml );
                email.setCharset( mailDetails.charset );
                email.setSubject( mailDetails.subject );
                email.setFrom( mailDetails.from.email, mailDetails.from.name );
                if ( mailDetails.replyTo != null ){
                    email.addReplyTo( mailDetails.replyTo.email, mailDetails.replyTo.name );
                }



                mailDetails.to.foreach( new AddMailerAction( RecipientType.TO, email ) );
                mailDetails.cc.foreach( new AddMailerAction( RecipientType.CC, email ) );
                mailDetails.bcc.foreach( new AddMailerAction( RecipientType.BCC, email ) );
                mailDetails.headers.foreach( new F.Function<Header, Void>() {
                    @Override
                    public Void apply( Header header ) throws Throwable
                    {
                        email.addHeader( header.name, header.value );
                        return null;
                    }
                } );

                email.setHostName( smtpConf.host );
                email.setSmtpPort( smtpConf.port );
                email.setSSL( smtpConf.ssl );
                email.setTLS( smtpConf.tls );
                if ( smtpConf.auth ) {
                    email.setAuthentication( smtpConf.user, smtpConf.password  );
                }
                email.setDebug( smtpConf.debug );
                email.send();
            } catch ( Exception e ) {
                // guy - we should consider adding here email recovery mechanism
                throw new RuntimeException( "unable to send email", e );

            }
        }

        /**
           * Creates an appropriate email object based on the content type.
           *
           * @param bodyText  -
           * @param bodyHtml  -
           * @return -
           */
        protected MultiPartEmail createEmailer( String bodyText, String bodyHtml ) throws EmailException
        {

            if ( StringUtils.isEmpty( bodyHtml ) ) {
                MultiPartEmail e = new MultiPartEmail();
                e.setMsg( bodyText );
                return e;
            } else if ( StringUtils.isEmpty( bodyText ) ) {
                return new HtmlEmail().setHtmlMsg( bodyHtml );
            } else {
                return new HtmlEmail().setHtmlMsg( bodyHtml ).setTextMsg( bodyText );
            }
        }
    }

    public static class GsMockMailer implements IMailer{

        private static Logger logger = LoggerFactory.getLogger(GsMockMailer.class);
        @Override
        public void send( GsMailConfiguration mailDetails )
        {
            logger.info( "sending email : {}" , mailDetails.toString() );
        }
    }

}
