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

	public String password;
	public String userId;
	public String apiKey;

}