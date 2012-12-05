package mocks;

import models.ServerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.DeployManager;
import server.ProcExecutor;

import java.io.File;

/**
 * ****************************************************************************
 * Copyright (c) 2010 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * The license agreement granted to you by GigaSpaces.
 * User: guym
 * Date: 12/2/12
 * Time: 4:54 PM
 * *****************************************************************************
 */
public class DeployManagerMock implements DeployManager {
    private static Logger logger = LoggerFactory.getLogger(DeployManagerMock.class);

    public ProcExecutor fork( ServerNode server, File recipe )
    {
        return null;
    }

    public ProcExecutor getExecutor( String id )
    {
        return null;
    }

    public void destroyExecutor(String id) {
        logger.info("destroying executor : ", id);
    }
}
