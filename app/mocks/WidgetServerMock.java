package mocks;

import models.Widget;
import models.WidgetInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.WidgetServer;

/**
 * ****************************************************************************
 * Copyright (c) 2010 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * The license agreement granted to you by GigaSpaces.
 * User: guym
 * Date: 12/2/12
 * Time: 5:00 PM
 * *****************************************************************************
 */
public class WidgetServerMock implements WidgetServer {
    private static Logger logger = LoggerFactory.getLogger(WidgetServerMock.class);
    public WidgetInstance deploy(String apiKey) {
        logger.info("deploying : " + apiKey);
        return null;
    }

    public void undeploy(String instanceId) {
        logger.info("undeploying : " + instanceId);
    }

    public Widget.Status getWidgetStatus(String instanceId) {
        logger.info("getting widget status : " + instanceId);
        return null;
    }
}
