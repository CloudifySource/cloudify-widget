package beans.cloudbootstrap;

import models.ServerNode;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/11/14
 * Time: 11:39 AM
 */
public interface ICloudProviderCreator {

    public File createCloudProvider( ServerNode serverNode );
}
