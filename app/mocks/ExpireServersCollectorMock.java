package mocks;

import models.ServerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.ExpiredServersCollector;

/**
 * ****************************************************************************
 * Copyright (c) 2010 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * The license agreement granted to you by GigaSpaces.
 * User: guym
 * Date: 12/2/12
 * Time: 4:58 PM
 * *****************************************************************************
 */
public class ExpireServersCollectorMock implements ExpiredServersCollector {
    private static Logger logger = LoggerFactory.getLogger(ExpireServersCollectorMock.class);

    public void scheduleToDestroy(ServerNode server) {
        logger.info("schedule destroy for : " + server.toDebugString() );
    }
}
