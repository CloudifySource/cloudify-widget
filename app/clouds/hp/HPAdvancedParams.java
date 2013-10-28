package clouds.hp;

import clouds.base.AdvancedParams;

/**
 * JSON example:
 	{"type": "hpcloud-compute",
	 "params": {"project":"il...-project", "key":"V7...", "secretKey":"bR.........O6"}
	}

 * @author evgenyf
 *
 */
public class HPAdvancedParams implements AdvancedParams{

	String project;
	String apiKey;
	String key;
	String secretKey;
	
	public HPAdvancedParams(){}

	public String getApiKey() {
		return apiKey;
	}

	public String getProject() {
		return project;
	}
	
	public String getKey() {
		return key;
	}	
	
    public String getSecretKey(){
        return secretKey;
    }	
}