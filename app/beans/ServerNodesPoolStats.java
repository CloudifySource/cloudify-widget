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

package beans;

/**
 * User: guym
 * Date: 2/27/13
 * Time: 9:41 AM
 */
public class ServerNodesPoolStats {
    public int all = -1;
    public int nonRemote = -1;
    public int busyServers = -1;
    public int nonBusyServers = -1;
    public int minLimit = -1;
    public int maxLimit = -1;


    public boolean isBelowLimit(){
        return nonBusyServers < minLimit;
    }

    @Override
    public String toString()
    {
        return "ServerNodesPoolStats{" +
                "all=" + all +
                ", nonRemote=" + nonRemote +
                ", busyServers=" + busyServers +
                ", nonBusyServers=" + nonBusyServers +
                ", minLimit=" + minLimit +
                ", maxLimit=" + maxLimit +
                '}';
    }
}
