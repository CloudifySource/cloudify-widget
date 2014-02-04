package cloudify.widget.api.clouds;

import java.util.Map;

import com.google.common.collect.Multimap;

/**
 * 
 * @author evnmgenyf
 * Date: 10/7/13
 */
public interface CloudServer {

	   /**
	    * @return the ip addresses assigned to the server
	    */
	   Multimap<String, CloudAddress> getAddresses();
	   /**
	    * 
	    * @return id
	    */
	   String getId();
	   Map<String, String> getMetadata();
	   String getName();
	   CloudServerStatus getStatus();
}