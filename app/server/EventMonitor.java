/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
