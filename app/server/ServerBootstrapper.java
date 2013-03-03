/*******************************************************************************
 * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package server;

import java.util.List;

import beans.BootstrapValidationResult;
import models.ServerNode;

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
    public List<ServerNode> createServers(int numOfServers);

    public void destroyServer(String serverId);

    /**
     *
     *
     * @param serverNode - the server we need to check
     * @return true iff bootstrap was a success
     */
    public BootstrapValidationResult validateBootstrap( ServerNode serverNode );

    public void close();
    
    public ServerNode bootstrapCloud( ServerNode serverNode );

    public List<ServerNode> recoverUnmonitoredMachines();
}
