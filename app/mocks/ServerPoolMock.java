package mocks;

import models.ServerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.ServerPool;

/**
 * ****************************************************************************
 * Copyright (c) 2010 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * The license agreement granted to you by GigaSpaces.
 * User: guym
 * Date: 12/2/12
 * Time: 4:59 PM
 * *****************************************************************************
 */
public class ServerPoolMock implements ServerPool {
    private static Logger logger = LoggerFactory.getLogger( ServerPoolMock.class );

    public ServerNode get()
    {
        logger.info( "getting server node" );
        return null;
    }

    public void destroy( String serverId )
    {
        logger.info( "destroying : %s " , serverId );
    }
}
