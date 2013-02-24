package org.cloudifysource.widget.beans;

/**
 * Created with IntelliJ IDEA.
 * User: sagib
 * Date: 15/01/13
 * Time: 11:41
 * To change this template use File | Settings | File Templates.
 */
public class TestConf {
    public String host;
    private int numOfMachines;
    private String serviceUrlPostFix;
    private String service;
    private String providerOrAPI;
    private String identity;
    private String credential;
    private String managementMachineId;
    private String location;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getNumOfMachines() {
        return numOfMachines;
    }

    public void setNumOfMachines(int numOfMachines) {
        this.numOfMachines = numOfMachines;
    }

    public String getServiceUrlPostFix() {
        return serviceUrlPostFix;
    }

    public void setServiceUrlPostFix(String serviceUrlPostFix) {
        this.serviceUrlPostFix = serviceUrlPostFix;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getProviderOrAPI() {
        return providerOrAPI;
    }

    public void setProviderOrAPI(String providerOrAPI) {
        this.providerOrAPI = providerOrAPI;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public String getManagementMachineId() {
        return managementMachineId;
    }

    public void setManagementMachineId(String managementMachineId) {
        this.managementMachineId = managementMachineId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
