package mocks;

import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.MailSender;

/**
 * User: guym
 * Date: 3/1/13
 * Time: 12:58 PM
 */
public class MailSenderMock implements MailSender {
    private static Logger logger = LoggerFactory.getLogger( MailSenderMock.class );
    @Override
    public void resetPasswordMail( User user )
    {
        logger.info( "sending reset password email" );
    }

    @Override
    public void sendPoolIsEmptyMail( String stats )
    {
        logger.info( "sending pool is empty email" );

    }
}
