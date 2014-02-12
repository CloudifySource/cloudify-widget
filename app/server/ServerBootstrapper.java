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

package server;

import java.util.List;


import beans.BootstrapValidationResult;
import models.ServerNode;
import server.exceptions.BootstrapException;

/**
 * The main abstraction to bootstrap servers on a cloud.
 *
 * The interface allows to
 * <ul>
 *  <li>create and boostrap servers</li>
 *  <li>get list of running and bootstrapped servers </li>
 *  <li>destroy a specific server</li>
 *  <li>close the context of ServerBoostrap implementation</li>
 * </ul>
 * 
 * @author Igor Goldenberg
 */
public interface ServerBootstrapper 
{
    /**
     * Returns numOfServers serverNodes with localcloud cloudify bootstrap on them.
     * Might return less if were unable to create all server nodes
     * @param numOfServers - the number of servers to create
     * @return a list of new servers.
     */
    public List<ServerNode> createServers(int numOfServers);

    public void destroyServer(ServerNode serverNode);

    // used when we don't have a ServerNode in the DB
    public void deleteServer( String nodeId );

    /**
     *
     *
     * @param serverNode - the server we need to check
     * @return true iff bootstrap was a success
     */
    public BootstrapValidationResult validateBootstrap( ServerNode serverNode );

    public void close();
    
    public ServerNode bootstrapCloud( ServerNode serverNode ) throws BootstrapException;

    public List<ServerNode> recoverUnmonitoredMachines();



    /**
     *   Take this machine and make it work.
     *   @return true iff you succeed.
     **/
    boolean reboot(ServerNode serverNode);


}
