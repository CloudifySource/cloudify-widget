package beans;

/**
 * User: guym
 * Date: 3/1/13
 * Time: 3:59 AM
 */
public class BootstrapValidationResult {
    Boolean machineReachable = true;
    Boolean managementAvailable = null;
    public Exception machineReachableException = null;

    public boolean getResult()
    {
        return checkTrue( machineReachable, managementAvailable );
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

    private boolean noNulls( Boolean ... args){
        for ( Boolean arg: args ){
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
        return noNulls( machineReachable, managementAvailable );
    }

    @Override
    public String toString()
    {
        return "BootstrapValidationResult{" +
                "machineReachable=" + machineReachable +
                ", managementAvailable=" + managementAvailable +
                ", machineReachableException=" + excString( machineReachableException ) +
                '}';
    }
}
