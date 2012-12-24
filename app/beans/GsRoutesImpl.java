/*
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
 */

package beans;

import server.GsRoutes;

/**
 * User: guym
 * Date: 12/16/12
 * Time: 8:05 AM
 */
public class GsRoutesImpl implements GsRoutes {
    public String widgetsRoute = "/admin/widgets.html";



    @Override
    public String getWidgetsRoute()
    {
        return widgetsRoute;
    }

    public void setWidgetsRoute( String widgetsRoute )
    {
        this.widgetsRoute = widgetsRoute;
    }
}
