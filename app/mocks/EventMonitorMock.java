/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
 */

package mocks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.EventMonitor;

/**
 * User: guym
 * Date: 1/14/13
 * Time: 1:30 PM
 */
public class EventMonitorMock implements EventMonitor {
    private static Logger logger = LoggerFactory.getLogger( EventMonitorMock.class );
    @Override
    public void eventFired( Event event )
    {
        logger.info( "firing event : " + event.asString() );
    }

    @Override
    public void updateUser( UpdateUserEvent event )
    {
        logger.info( "updating user : " + event.asString() );
    }

    @Override
    public void auditUserAction( AuditActionEvent event )
    {
        logger.info( "auditing user action : " + event.asString()  );
    }
}
