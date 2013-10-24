package clouds.softlayer;

import clouds.base.AdvancedParams;

/**
 * JSON example:
   {"type": "softlayer",
	"params": { "userId":"SL...", "project":"evgeny", "secretKey":"....." }
	}

 * @author evgenyf
 *
 */
public class SoftlayerAdvancedParams implements AdvancedParams{

	String project;
	String userId;
	String secretKey;
	
	public SoftlayerAdvancedParams(){}

	public String getProject() {
		return project;
	}
	
	public String getUserId() {
		return userId;
	}	
	
    public String getSecretKey(){
        return secretKey;
    }	
}