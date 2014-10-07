package mocks.cloud;


import cloudify.widget.api.clouds.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/5/14
 * Time: 1:56 PM
 */
public class CloudServerApiMock implements CloudServerApi {
    private static Logger logger = LoggerFactory.getLogger(CloudServerApiMock.class);
    @Override
    public Collection<CloudServer> getAllMachinesWithTag(String s) {
        logger.info("getting all machines with tag " + s);
        return new LinkedList<CloudServer>();
    }

    @Override
    public CloudServer get(String s) {
        logger.info("getting server " + s );
        return null;
    }

    @Override
    public void delete(String s) {
        logger.info("deleting server " + s);
    }

    @Override
    public void rebuild(String s) {
        logger.info("rebuilding server " + s );

    }

    @Override
    public Collection<? extends CloudServerCreated> create(MachineOptions machineOptions) {
        logger.info("creating machine with opts " + machineOptions.toString() );
        return null;
    }

    @Override
    public String createCertificate() {
        logger.info("creating certificate");
        return null;
    }


    @Override
    public void createSecurityGroup(ISecurityGroupDetails securityGroupDetails) {
        logger.info("creating security group" + securityGroupDetails.toString() );
    }

    @Override
    public CloudExecResponse runScriptOnMachine(String script, String serverIp, ISshDetails iSshDetails) {
        logger.info("running script on machine " + serverIp + " with ssh details " + iSshDetails.toString() );
        return null;
    }

    @Override
    public void setConnectDetails(IConnectDetails connectDetails) {

    }

    @Override
    public CloudExecResponse runScriptOnMachine(String s, String s2, ISshDetails iSshDetails, ISshOutputHandler outputHandler) {
        return null;
    }

    @Override
    public void connect() {

    }

    @Override
    public void connect(IConnectDetails connectDetails) {
        logger.info("connecting");
    }
}
