package server;

import models.Widget;
import models.WidgetInstance;

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
public interface WidgetServer {
    WidgetInstance deploy(String apiKey);

    void undeploy(String instanceId);

    Widget.Status getWidgetStatus(String instanceId);
}
