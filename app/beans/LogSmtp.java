/*
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
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
