package cloudify.widget.api.clouds;

/**
 * 
 * @author evgenyf
 * Date: 10/7/13
 */
public enum CloudServerStatus {

    ACTIVE, BUILD, REBUILD, SUSPENDED, PAUSED, RESIZE, VERIFY_RESIZE, REVERT_RESIZE, 
    PASSWORD, REBOOT, HARD_REBOOT, DELETED, UNKNOWN, ERROR, STOPPED, UNRECOGNIZED;

    public String value() {
       return name();
    }

    public static CloudServerStatus fromValue(String v) {
       try {
          return valueOf(v.replaceAll("\\(.*", ""));
       } catch (IllegalArgumentException e) {
          return UNRECOGNIZED;
       }
    }
 }