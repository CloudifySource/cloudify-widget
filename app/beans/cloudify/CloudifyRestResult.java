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

package beans.cloudify;

import org.apache.commons.exec.util.StringUtils;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * User: guym
 * Date: 4/24/13
 * Time: 12:40 PM
 */
public class CloudifyRestResult {
    public Status status;
    @JsonProperty( "error_args" )
    public List<String> errorArgs = new LinkedList<String>(  );
    public String error;

    public static enum Status{
        success, error
    }

    public boolean isSuccess(){
        return status == Status.success;
    }

    public static class ListServices extends CloudifyRestResult{
        public List<String> response;
    }

    public static class TestRest extends CloudifyRestResult{
        // nothing, empty body
    }

//    {"Cloud Public IP":"15.185.182.232"}
    public static class GetPublicIpResult{
        @JsonProperty(value = "Cloud Public IP")
        public String cloudPublicIp;
    }

    public static class GetVersion extends CloudifyRestResult{

        public static final String DUMMY_VERSION="XXX"; // we need to send some false version to cloudify.

        public String getVersion(){
            for ( String errorArg : errorArgs ) {
                if ( !DUMMY_VERSION.equals( errorArg) ){
                    return StringUtils.split(errorArg,"-")[0];
                }
            }
            throw new RuntimeException( String.format("unable to decipher cloudify version from errorArgs [%s]", errorArgs) );
        }
    }

    public static class ListApplications extends CloudifyRestResult{
        public Map<String, String> response;
    }
}
