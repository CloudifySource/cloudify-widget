package org.cloudifysource.widget.test;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: sagib
 * Date: 06/01/13
 * Time: 16:41
 */

public class UserTest extends AbstractCloudifyWidgetTest {
    private static Logger logger = LoggerFactory.getLogger(UserTest.class);


    @Test
    public void subscribeTest(){
        logger.info("starting subscribe test");
        assertUserIsLoggedIn();
    }


    @Test
    public void logoutLoginTest(){
        logger.info("starting logout / login test");
        logout();
        assertLoggedOut();
        login(EMAIL, PASSWORD);
        assertUserIsLoggedIn();
    }

    @Test
    public void changePasswordTest(){
        logger.info("starting change password test");
        String newPassword = PASSWORD + 2;
        changePassword(PASSWORD, newPassword);
        logout();
        login(EMAIL, newPassword);
        assertUserIsLoggedIn();
        changePassword(newPassword, PASSWORD);
        assertUserIsLoggedIn();
    }


}
