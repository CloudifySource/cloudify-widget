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

package mocks;

import models.ServerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.DeployManager;
import server.ProcExecutor;

import java.io.File;

public class DeployManagerMock implements DeployManager 
{
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
