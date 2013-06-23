package beans;

import beans.config.CloudProvider;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/13/13
 * Time: 9:45 AM
 */
public interface CloudCredentials {
    public CloudProvider getCloudProvider();

    /**
     *
     * @see org.jclouds.ContextBuilder#credentials(String, String)
     * @return the identity required by jclouds' context builder
     */
    public String getIdentity();

    /**
     * @see org.jclouds.ContextBuilder#credentials(String, String)
     * @return the credential required by jclouds' context builder
     */
    public String getCredential();
}
