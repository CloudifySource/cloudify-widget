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
import models.Widget;
import models.WidgetInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.DeployManager;

public class DeployManagerMock implements DeployManager 
{
    private static Logger logger = LoggerFactory.getLogger(DeployManagerMock.class);


    @Override
    public WidgetInstance fork( ServerNode server, Widget widget )
    {
        logger.info( "forking [{}] : [{}]" , server, widget );
        return null;
    }

    @Override
    public WidgetInstance uninstall( ServerNode server )
    {
        logger.info( "uninstalling [{}]", server );
        return null;
    }

    @Override
    public String getServicePublicIp( WidgetInstance widgetInstance )
    {
        logger.info( "getting service public IP for [{}]", widgetInstance );
        return null;
    }
}
