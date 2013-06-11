package beans.config;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/11/13
 * Time: 2:17 PM
 */
public enum CloudProvider {
    HP("hpcloud-compute");

    public String label;

    private CloudProvider( String label ) {
        this.label = label;
    }
}
