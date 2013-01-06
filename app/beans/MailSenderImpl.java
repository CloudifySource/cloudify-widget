package beans;

import beans.config.Conf;
import controllers.routes;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.mvc.Call;
import play.mvc.Http;
import server.ApplicationContext;
import server.Hmac;
import server.MailSender;
import beans.GsMailer.GsMailConfiguration;

import javax.inject.Inject;
import java.net.URLEncoder;

/**
 * User: guym
 * Date: 12/14/12
 * Time: 11:20 AM
 */
public class MailSenderImpl implements MailSender {

    @Inject
    private Conf conf;

    @Inject
    private Hmac hmac;

    private static Logger logger = LoggerFactory.getLogger( MailSenderImpl.class );

    @Override
    public void resetPasswordMail( User user ){

        logger.info( "user {} requested password reset", user.toDebugString() );
//        String link = routes.WidgetAdmin.getAllServers();


        String encode;
        String sign = hmac.sign( user.getEmail(), user.getId(), user.getPassword() );
        try{
            encode = URLEncoder.encode( sign, "UTF-8" );
        }catch(Exception e){
            throw new RuntimeException( String.format( "unable to url encode hmac [%s]", sign ) , e );
        }
        Call call = routes.WidgetAdmin.resetPasswordAction( encode, user.getId() );

        // guy - this is not nice. we have to have the request for the absoluteURL.
        // we should find a way to fake the request, just in case we have a job that sends emails with links.
        String link = call.absoluteURL( Http.Context.current().request() );

        String mailContent = views.html.mail.resetPassword.render( user, link ).body();
        GsMailConfiguration mConf = new GsMailConfiguration();
        mConf.addRecipient( GsMailer.RecipientType.TO, user.getEmail(), user.getFullName() )
                .setBodyHtml( mailContent )
                .setBodyText( mailContent )
                .setFrom( conf.smtp.user, conf.mailer.name )
                .setReplyTo( conf.mailer )
                .setSubject( "Reset Password" );

        ApplicationContext.get().getMailer().send(mConf);
    }

    public void setConf( Conf conf )
    {
        this.conf = conf;
    }

    public void setHmac( Hmac hmac )
    {
        this.hmac = hmac;
    }
}
