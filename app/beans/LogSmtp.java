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

package beans;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.apache.commons.io.FileUtils;
import server.ApplicationContext;

import java.io.*;

/**
 * User: guym
 * Date: 12/16/12
 * Time: 10:11 AM
 */
public class LogSmtp extends AppenderBase<ILoggingEvent> {


    @Override
    protected void append( ILoggingEvent eventObject )
    {
        try {
            if ( ApplicationContext.get().conf().sendErrorEmails ) {
                GsMailer.GsMailConfiguration mail = new GsMailer.GsMailConfiguration();
                // TODO : move configuration to configuration files.
                // TODO : add dev mode support
                mail.setSubject( "Error occured" );
                mail.addRecipient( GsMailer.RecipientType.TO, "guym@gigaspaces.com", "Guy Mograbi" );
                mail.setBodyText( eventObject.getFormattedMessage() );
                mail.setFrom( "it@gigaspaces.com", "gigaspaces" );
                ApplicationContext.get().getMailer().send( mail );
            }
        } catch ( Exception e ) {
            Writer writer = new StringWriter();
            e.printStackTrace( new PrintWriter( writer ) );
            // if we write errors to log here, we will end up here again.. infinite loop.. :(
            File file = new File( "appender_problems_" + System.currentTimeMillis() + ".error" );
            try {
                FileUtils.writeStringToFile( file, writer.toString() );
            } catch ( IOException e1 ) {
                System.out.println( "unable to print appender errors." );
            }

        }

    }
}
