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

import server.ApplicationContext;

/**
 * User: guym
 * Date: 3/1/13
 * Time: 3:59 AM
 */
public class BootstrapValidationResult {
    public Boolean machineReachable = true;
    public String managementVersion = null;
    public Boolean managementAvailable = null;
    public String lastComparedVersion = null;
    public Exception machineReachableException = null;
    public boolean applicationAvailable = true;

    private boolean getResult( String expectedVersion )
    {
        lastComparedVersion = expectedVersion;
        return checkTrue( machineReachable, applicationAvailable, managementAvailable, expectedVersion == null || expectedVersion.equals( managementVersion ) );
    }

    private String getDefaultCompareVersion (){
        return ApplicationContext.get().conf().cloudify.version;
    }

    public boolean isValidWithoutVersion(){
        return getResult( null );
    }

    public boolean isValid(){
        return getResult( getDefaultCompareVersion() );
    }

    public boolean isValid( String version ){
        return testCompleted() && getResult( version );
    }

    private boolean checkTrue( Boolean ... args ){
        for ( Boolean arg : args ) {
            if ( arg == Boolean.FALSE )
            {
                return false;
            }
        }
        return true;
    }

    private boolean noNulls( Object ... args){
        for ( Object arg: args ){
            if ( arg == null ){
                return false;
            }
        }
        return true;
    }

    public String excString( Exception e ){
        return e == null ? "null" : e.getMessage();
    }

    public boolean testCompleted(){
        return noNulls( machineReachable, managementAvailable, managementVersion );
    }

    @Override
    public String toString()
    {
        return "BootstrapValidationResult{" +
                "machineReachable=" + machineReachable +
                ", managementAvailable=" + managementAvailable +
                ", managementVersion=" + managementVersion +
                ", applicationAvailable=" + applicationAvailable +
                ", conf version=" + getDefaultCompareVersion() +
                ", last compared version = " + lastComparedVersion +
                ", machineReachableException=" + excString( machineReachableException ) +
                '}';
    }
}
