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
package beans.api;

import java.io.File;

import models.ServerNode;
import server.ProcExecutor;

/**
 * Executor factory interface.
 * 
 * @author adaml
 *
 */
public interface ExecutorFactory {

	/**
	 * returns an executor for performing bootstrap to the cloud.
	 * 
	 * @param key The key that the executor uses to write to the play cache.
	 * @return process executor.
	 */
	ProcExecutor getBootstrapExecutor( String key );
	
	/**
	 * returns an executor for deploying a service or application on the cloud.
	 * @param server the server node.
	 * @param recipe the recipe to deploy
	 * @param args
	 * @return process executor.
	 */
	ProcExecutor getDeployExecutor( ServerNode server, File recipe, String ... args );
	
}
