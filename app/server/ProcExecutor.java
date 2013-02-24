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

import org.apache.commons.exec.Executor;

/**
 * The main abstraction of the forked process.
 *
 * The interface allows to
 * <ul>
 *  <li>get the IP of the deployed server</li>
 *  <li>get the output of the forked process</li>
 *  <li>get the elapsed time before the process will be destroyed</li>
 * </ul>
 * 
 * @author Igor Goldenberg
 * @see ProcExecutor
 */
public interface ProcExecutor extends Executor 
{
	public String getId();
	
	public void setId(String id);
	
}
