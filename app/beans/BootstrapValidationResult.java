package beans;

import server.ApplicationContext;

/**
 * User: guym
 * Date: 3/1/13
 * Time: 3:59 AM
 */
public class BootstrapValidationResult {
    Boolean machineReachable = true;
    String managementVersion = null;
    Boolean managementAvailable = null;
    String lastComparedVersion = null;
    public Exception machineReachableException = null;

    private boolean getResult( String expectedVersion )
    {
        lastComparedVersion = expectedVersion;
        return checkTrue( machineReachable, managementAvailable, expectedVersion == null || expectedVersion.equals( managementVersion ) );
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
        return getResult( version );
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
                ", conf version=" + getDefaultCompareVersion() +
                ", last compared version = " + lastComparedVersion +
                ", machineReachableException=" + excString( machineReachableException ) +
                '}';
    }
}
