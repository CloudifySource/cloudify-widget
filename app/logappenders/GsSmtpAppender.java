package logappenders;

import beans.config.Conf;
import ch.qos.logback.classic.net.SMTPAppender;

/**
 * User: guym
 * Date: 11/21/13
 * Time: 1:12 PM
 */
public class GsSmtpAppender extends SMTPAppender{

    // stupid implementation but could not find a way around it.
    // we cannot rely on the fact that the appender is instantiated at a certain point,
    // we must initialize it when "start" is called. however, we cannot know when conf is instantiated.
    // and so there must be 2 flows that initialize the appender - one when we know that conf is initialized,
    // and another one when start is called. whoever is invoked last, wins.
    public static Conf conf = null;

    @Override
    public void start() {
        addInfo("called start");
        if ( conf != null ){
            addInfo("starting");
            initialize();
            super.start();
        }else{
            addInfo("not starting, no configuration");
        }
    }

    private void initialize() {
        addInfo("initializing");
        setPassword(conf.smtp.password);
        setSTARTTLS(conf.smtp.tls);
        setUsername(conf.smtp.user);
        setFrom(conf.smtp.user);
        setSMTPHost(conf.smtp.host);
        setSMTPPort(conf.smtp.port);

        String subject = getSubject();
        setSubject(conf.application.name + " " + subject);

        addTo(conf.mails.logErrors.email);
        addInfo("smtp appender initialized");
    }

}
