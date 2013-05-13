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

package beans.config;

/**
 * User: guym
 * Date: 12/13/12
 * Time: 1:33 PM
 */
public class SmtpConf {
    public String host = "N/A";
    @Config( ignoreNullValues = true )
    public int port = 0;
    @Config( ignoreNullValues = true )
    public boolean tls = false;
    @Config( ignoreNullValues = true )
    public boolean mock = false;
    @Config( ignoreNullValues = true )
    public boolean enabled = true;
    @Config( ignoreNullValues = true )
    public boolean debug = false;
    public String user = "N/A";
    public String password = "N/A";
    @Config( ignoreNullValues = true )
    public boolean ssl = false;
    @Config( ignoreNullValues = true )
    public boolean auth = false;
}
