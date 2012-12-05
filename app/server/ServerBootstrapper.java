package server;

import models.ServerNode;
import org.jclouds.openstack.nova.v2_0.domain.Server;

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
 * Time: 12:07 PM
 * *****************************************************************************
 */
public interface ServerBootstrapper {
    public List<Server> getServerList();

    public void destroyServer(String serverId);

    public List<ServerNode> createServers(int numOfServers);

    public void close();
}
