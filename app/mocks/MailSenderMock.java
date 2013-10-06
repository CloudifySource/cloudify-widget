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

package mocks;

import models.Lead;
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

    public void sendRegistrationMail( Lead lead ){
        logger.info( "sending registration mail");
    }

    @Override
    public void sendChangelog() {
        logger.info("sending changelog");
    }


}
