package mocks.cloud;

import cloudify.widget.api.clouds.*;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/5/14
 * Time: 1:56 PM
 */
public class CloudServerApiMock implements CloudServerApi {
    @Override
    public Collection<CloudServer> getAllMachinesWithTag(String s) {
        return new LinkedList<CloudServer>();
    }

    @Override
    public CloudServer get(String s) {
        return null;
    }

    @Override
    public boolean delete(String s) {
        return false;
    }

    @Override
    public void rebuild(String s) {

    }

    @Override
    public CloudServerCreated create(MachineOptions machineOptions) {
        return null;
    }

    @Override
    public String createCertificate() {
        return null;
    }

    @Override
    public void createSecurityGroup() {

    }

    @Override
    public CloudServerCreated create(String s, String s2, String s3, CloudCreateServerOptions... cloudCreateServerOptionses) throws RunNodesException {
        return null;
    }
}
