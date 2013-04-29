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

import models.ServerNode;
import models.Widget;
import models.WidgetInstance;

import java.io.File;

/**
 * The main abstraction to deploy recipe.
 *
 * The interface allows to
 * <ul>
 *  <li>to fork process on specific Server</li>
 *  <li>get the process executor of the forked process and get the output</li>
 *  <li>destroy process executor</li>
 * </ul>
 * 
 * @author Igor Goldenberg
 * @see ProcExecutor
 */
public interface DeployManager 
{
   public WidgetInstance fork( ServerNode server, Widget widget );

    public WidgetInstance uninstall( ServerNode server);
}
