package beans.config;

import beans.GsMailer;
import utils.Utils;

import java.io.File;

/**
 * User: guym
 * Date: 12/13/12
 * Time: 1:32 PM
 */
public class Conf {

    public ApplicationConfiguration application = new ApplicationConfiguration();

    public SmtpConf smtp = new SmtpConf();

    // who is sending the mail?
    public GsMailer.Mailer mailer = new GsMailer.Mailer();

    public WidgetConfiguration widget = new WidgetConfiguration();

    public ServerConfig server = new ServerConfig();

    @Config(ignoreNullValues = true)
    public boolean sendErrorEmails = false;

    public CloudifyConfiguration cloudify = new CloudifyConfiguration();




    public static class WidgetConfiguration{

        public String serverId;

        public long stopTimeoutMillis;
    }

    public static class CloudifyConfiguration{

        public long deployWatchDogProcessTimeoutMillis = Utils.parseTimeToMillis( "2mn" );

        public File deployScript;

        public String removeOutputLines = "";

        public String removeOutputString = "";
    }


}
