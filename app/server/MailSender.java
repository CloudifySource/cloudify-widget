package server;

import models.User;

/**
 * User: guym
 * Date: 12/19/12
 * Time: 11:27 AM
 */
public interface MailSender {
    public void resetPasswordMail( User user );

    public void sendPoolIsEmptyMail();
}
