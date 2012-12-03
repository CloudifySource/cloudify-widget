package mocks;

import models.ServerNode;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.ServerBootstrapper;

import java.util.List;

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
public class ServerBootstrapperMock implements ServerBootstrapper {
    private static Logger logger = LoggerFactory.getLogger(ServerBootstrapperMock.class);

    public List<Server> getServerList() {
        logger.info("getting server list");
        return null;
    }

    public void destroyServer(String serverId) {
        logger.info("destroying server",serverId);

    }

    public List<ServerNode> createServers(int numOfServers) {
        logger.info("creating servers", numOfServers);
        return null;
    }

    public void close() {
        logger.info("closing");
    }
}
