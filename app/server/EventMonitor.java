/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
 */

package server;

import models.User;
import org.json.JSONObject;

import java.util.Map;

/**
 * User: guym
 * Date: 1/14/13
 * Time: 1:29 PM
 */
public interface EventMonitor {

    public void eventFired( Event event );

    public void updateUser( UpdateUserEvent event );

    public void auditUserAction( AuditActionEvent event );

    public static interface Event{

        public String getDistinctId();

        public String getName();

        public JSONObject getProperties();

        public String asString();
    }


    public static interface UpdateUserEvent{

        public User getUser();

        public JSONObject getProperties();

        public String asString();
    }

    public static interface AuditActionEvent{

        public Map<String,Long> getProperties();

        public User getUser();

        public String asString();
    }
}
