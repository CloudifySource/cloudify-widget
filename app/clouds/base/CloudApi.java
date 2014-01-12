package clouds.base;

/**
 * 
 * @author evgenyf
 * Date: 10/7/13
 */
public interface CloudApi {

	/**
	 * 
	 * @param zone
	 * @return CloudServerApi
	 */
	CloudServerApi getServerApiForZone( String zone );
}