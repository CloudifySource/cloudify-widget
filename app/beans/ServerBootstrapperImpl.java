/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package beans;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;


import beans.cloudbootstrap.CloudProviderCreatorFactory;
import beans.cloudbootstrap.ICloudProviderCreator;
import beans.cloudify.CloudifyRestClient;
import beans.config.ServerConfig;
import beans.scripts.ScriptExecutor;
import beans.scripts.ScriptFilesUtilities;
import cloudify.widget.api.clouds.*;
import cloudify.widget.cli.ICloudifyCliHandler;
import cloudify.widget.common.StringUtils;
import cloudify.widget.common.asyncscriptexecutor.IAsyncExecution;
import models.CreateMachineOutput;
import models.ServerNode;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.libs.Json;
import play.libs.WS;
import server.ApplicationContext;
import server.ServerBootstrapper;
import server.exceptions.ServerException;
import utils.CollectionUtils;
import utils.ResourceManagerFactory;
import utils.Utils;

import javax.inject.Inject;

/**
 *
 * This class bridges between {@link CloudServer} and {@link ServerNode} and allows the rest of the website
 * to deal only with the model.
 *
 * It mediates machine create/destroy and bootstrap operations between the website the cloud driver.
 *
 * This class manages a compute cloud provider.
 * It provides ability to create/delete specific server with desired flavor configuration.
 * On each new server runs a bootstrap script that prepare machine for a server-pool,
 * it includes a setup of firewall, JDK, cloudify installation and etc...
 * The bootstrap script can be found under ssh/bootstrap_machine.sh
 *
 *
 * @author Igor Goldenberg
 *
 *
 *
 *
 */
public class ServerBootstrapperImpl implements ServerBootstrapper
{

    private static Logger logger = LoggerFactory.getLogger( ServerBootstrapperImpl.class );

    @Inject
    private CloudServerApi cloudServerApi;

    @Inject
    private MachineOptions bootstrapMachineOptions;

    @Inject
    private CloudifyRestClient cloudifyRestClient;

    @Inject
    private ICloudifyCliHandler cliHandler;

    @Inject
    private ScriptExecutor scriptExecutor;

    @Inject
    private ResourceManagerFactory resourceManagerFactory;

    /**
     * The machine tag is a unique identifier to all machines related to this widget instance.
     * It can manifest in many ways on the cloud - tag in hp-cloud, machine name in softlayer etc..
     * as long as there's a way to get all machines by it, it is fine.
     */

    private ServerConfig.BootstrapConfiguration bootstrapConf;

    private ServerConfig.CloudBootstrapConfiguration cloudBootstrapConf;

    @Override
    public List<ServerNode> createServers(int numOfServers) {
        logger.info("creating [{}] new server", numOfServers);
        List<ServerNode> newServers = new LinkedList<ServerNode>();
        for ( int i = 0; i < numOfServers; i ++ ){
            ServerNode serverNode = createServer();
            if ( serverNode != null ){
                newServers.add(serverNode);
            }
        }
        return newServers;
    }


    private ServerNode createServer()
    {

        ServerNode serverNode = null;
        int retries = bootstrapConf.createServerRetries;
        for ( int i =0 ; i <  retries && serverNode == null ; i++){
            logger.info( "creating new server node, try #[{}/{}]",i, retries );

            // guy - this try/catch is very important as this function is used as part of creation of N servers.
            // if we throw an exception here, we might miss the other N-1 servers as well.
            // another reason this try catch is very important is the "undergoing bootstrap" count which relies
            // on the collection size we return from "createServer(N)" - if we throw an exception here, we will
            // miss the "decreaseAndGet" invocation, and the pool state will not longer by correct.
            // as we will have a permanent "in progress" server.
            try {
                final CloudServerCreated createdServer = CollectionUtils.first(cloudServerApi.create(bootstrapMachineOptions));
//            PoolEvent.ServerNodeEvent newServerNodeEvent = new PoolEvent.ServerNodeEvent().setType(PoolEvent.Type.CREATE).setServerNode(tmpNode);
                if (createdServer != null) {
                    final CloudServerApi finalCloudServerApi = cloudServerApi;
                    CloudServer server = cloudServerApi.get(createdServer.getId());
                    ServerNode tmpNode = new ServerNode(server);
                    tmpNode.setRandomPassword(StringUtils.generateRandomFromRegex(ApplicationContext.get().conf().server.bootstrap.serverNodePasswordRegex));
                    final ActiveWait wait = new ActiveWait();

                    wait
                            .setIntervalMillis(TimeUnit.SECONDS.toMillis(10))
                            .setTimeoutMillis(TimeUnit.SECONDS.toMillis(120))
                            .waitUntil(new Wait.Test() {
                                @Override
                                public boolean resolved() {
                                    logger.info("Waiting for a server activation... Left timeout: {} sec", wait.getTimeLeftMillis() / 1000);
                                    return finalCloudServerApi.get(createdServer.getId()).isRunning();
                                }
                            });

                    BootstrapResponse bootstrap = bootstrap(tmpNode);
                    if (bootstrap.success) { // bootstrap success
//                    poolEventManager.handleEvent(newServerNodeEvent);
                        serverNode = tmpNode;
                        logger.info("successful bootstrap on [{}]", serverNode);
                    } else { // bootstrap failed
                        logger.info("bootstrap failed, deleting server");

                        try{
                            CreateMachineOutput output = new CreateMachineOutput();
                            output.setCurrentTry(i);
                            output.setMaxTries(retries);
                            output.setCreated(System.currentTimeMillis());
                            output.setException(ExceptionUtils.getFullStackTrace(bootstrap.e));
                            output.setOutput(bootstrap.response.getOutput());
                            output.setExitCode(bootstrap.response.getExitStatus());
                            output.save();
                        }catch(Exception e){
                            logger.error("unable to save create machine output",e);
                        }

                        deleteServer(tmpNode.getNodeId()); // deleting the machine from cloud.
                    }
                } else { // create server failed
                    logger.info("unable to create machine. try [{}/{}]", i + 1, retries);
                }
            }catch(Exception e){
                logger.info("error creating server",e);
            }

        }

        if ( serverNode == null ){
            logger.error("create server failed " + retries + " times. need to alert user about this");
        }
        return serverNode;

    }


    // teardown to remote machine
    public void teardown( ServerNode serverNode ){

    }

    public static class BootstrapResponse{
        public CloudExecResponse response = null;
        public boolean success = false;
        public Exception e;
    }


    private BootstrapResponse bootstrap( ServerNode serverNode ){
        BootstrapResponse response = new BootstrapResponse();
        long timeout = bootstrapConf.sleepBeforeBootstrapMillis;

        logger.info("Server created, wait {} milliseconds before starting to bootstrap machine: {}", timeout, serverNode.getPublicIP());

        Utils.threadSleep(timeout);
        // bootstrap machine: firewall, jvm, start cloudify
        try {
            response.response = bootstrapMachine(serverNode);

            BootstrapValidationResult bootstrapValidationResult = validateBootstrap(serverNode);

            if (bootstrapValidationResult.isValid()) {
                response.success = true;
            }
        } catch (RuntimeException e) {
            response.e = e;
        }
        return response;
    }

    @Override
    public void destroyServer(ServerNode serverNode) {
        if ( serverNode == null ){
            return;
        }
        logger.info("destroying server [{}]", serverNode);

        try{
            logger.info("reading script from file [{}]", bootstrapConf.teardownScript);
            String script = FileUtils.readFileToString(bootstrapConf.teardownScript);
            CloudExecResponse response = cloudServerApi.runScriptOnMachine( script, serverNode.getPublicIP(), null );
        }catch(Exception e){
            logger.info("unable to teardown.",e);
        }


        try {
            serverNode.setStopped(true);
            serverNode.save();
        } catch (Exception e) {
            logger.error("unable to set server as stop", e);
        }

        try{

            deleteServer(serverNode.getNodeId());
        }catch(Exception e){
            logger.info("unable to delete. perhaps this node will remain on the cloud. need to remove manually.");
        }



        if ( serverNode.getId() != null ){
            logger.info("deleting serverNode");
            try{
                serverNode.refresh();
                serverNode.delete();
            }catch(Exception e){
                logger.warn("unable to delete node [{}]", e.getMessage());
            }
        }
    }

    @Override
    public void deleteServer(String nodeId) {
        logger.info("destroying server [{}]", nodeId);

        cloudServerApi.delete(nodeId);
    }

    @Override
    public BootstrapValidationResult validateBootstrap(ServerNode serverNode) {

        logger.info("validating bootstrap on [{}]", serverNode );
        BootstrapValidationResult result = new BootstrapValidationResult();

        if ( result.machineReachable == Boolean.TRUE ){
            try{
                result.managementVersion = cloudifyRestClient.getVersion( serverNode.getPublicIP() ).getVersion();
                try{
                    if ( !StringUtils.isEmpty(bootstrapConf.bootstrapApplicationUrl) ){
                        WS.Response response = WS.url( String.format(bootstrapConf.bootstrapApplicationUrl, serverNode.getPublicIP() )).get().get();
                        result.applicationAvailable = response.getStatus() == 200;
                        logger.info("decided application is  available [{}] while responseStatus was [{}] on ip [{}]", result.applicationAvailable,  response.getStatus(), serverNode.getPublicIP() );
                    }
                }catch(Exception e){
                    logger.error("unable to determine if application is available or not",e);
                    result.applicationAvailable = false;
                }
                result.managementAvailable = true;
            }catch( Exception e ){
                logger.debug( "got exception while checking management version",e );
                result.managementAvailable = false;
            }
        }

        return result;
    }

    @Override
    public void close() {
       logger.info("closing");
    }



    @Override
    public ServerNode bootstrapCloud(ServerNode serverNode) {
        File newCloudFolder = null;
        try{
            logger.info("bootstrapping cloud with details [{}]", serverNode);
            ICloudProviderCreator creator = new CloudProviderCreatorFactory().getCreator(serverNode.getWidget().cloudProvider);
            newCloudFolder = creator.createCloudProvider(serverNode);

                        //Command line for bootstrapping remote cloud.
            CommandLine cmdLine =
            		new CommandLine( cloudBootstrapConf.remoteBootstrap.getAbsoluteFile() );
            cmdLine.addArgument( newCloudFolder.getName() );

            IAsyncExecution asyncExecution = scriptExecutor.runBootstrapScript(cmdLine, serverNode);

            // wait for bootstrap to complete and write output details (such as IP) to the serverNode;
            ScriptFilesUtilities.waitForFinishBootstrappingAndSaveServerNode( serverNode, asyncExecution );


            return serverNode;
//
        }catch(Exception e){
            logger.error("unable to bootstrap",e);
            return null;
        }  finally {
    		if (newCloudFolder != null && cloudBootstrapConf.removeCloudFolder ) {
    			FileUtils.deleteQuietly(newCloudFolder);
    		}
    		serverNode.setStopped(true);
    	}
    }

    @Override
    public List<ServerNode> recoverUnmonitoredMachines() {
        logger.info("recovering lost machines");
        List<ServerNode> result = new ArrayList<ServerNode>();

        // removing this code as it is not relevant anymore after the random value feature.
        // instead we should allow entering values manually

//        Collection<CloudServer> allMachinesWithTag = cloudServerApi.getAllMachinesWithTag(this.bootstrapMachineOptions.getMask());
//        logger.info("found [{}] total machines with matching tags. filtering lost", CollectionUtils.size(allMachinesWithTag));
//        if (!CollectionUtils.isEmpty(allMachinesWithTag)) {
//            for (CloudServer server : allMachinesWithTag) {
//                ServerNode serverNode = ServerNode.getServerNode(server.getId());
//                if (serverNode == null) {
//                    ServerNode newServerNode = new ServerNode(server);
//                    logger.info("found an unmonitored machine - I should add it to the DB [{}]", newServerNode);
//                    result.add(newServerNode);
//                }
//            }
//        }
        return result;
    }

    private CloudExecResponse bootstrapMachine( ServerNode server ){

		try
		{

            logger.info("Starting bootstrapping for server:{} " , server );
            String script = getInjectedBootstrapScript( server.getPublicIP(), server.getPrivateIP(), server.getRandomPassword() );

			CloudExecResponse response = cloudServerApi.runScriptOnMachine( script, server.getPublicIP(), null );

            logger.info("script finished");
			logger.info("Bootstrap for server: {} finished successfully successfully. " +
                    "ExitStatus: {} \nOutput:  {}", new Object[]{server,
                    response.getExitStatus(),
                    response.getOutput()} );
            return response;
		}catch(Exception ex)
		{
            logger.error("unable to bootstrap machine [{}]", server, ex);
            try{
                destroyServer( server );
            }catch(Exception e){
                logger.info("destroying server after failed bootstrap threw exception",e);
            }
			throw new ServerException("Failed to bootstrap cloudify machine: " + server.toDebugString(), ex);
		}

	}

    @Override
    public String getInjectedBootstrapScript(String publicIp, String privateIp, String randomPassword) {
        try {
            String prebootstrapScript = "";

            try {
                prebootstrapScript = FileUtils.readFileToString(bootstrapConf.prebootstrapScript);
            } catch (Exception e) {
                logger.error("error reading prebootstrapScript [{}]", bootstrapConf.prebootstrapScript);
                throw new RuntimeException("unable to find prebootstrapScript", e);
            }


            if ( bootstrapConf.cloudifyUrl == null ){

                throw new RuntimeException("Missing cloudify URL");

            }
            logger.info("reading script from file [{}]", bootstrapConf.script);
            String script = FileUtils.readFileToString(bootstrapConf.script);
            script = script.replace("##publicip##", publicIp)
                    .replace("##randomPassword##", randomPassword)
                    .replace("##privateip##", privateIp)
                    .replace("##recipeUrl##", bootstrapConf.recipeUrl)
                    .replace("##cloudifyUrl##", bootstrapConf.cloudifyUrl)
                    .replace("##urlAccessKey##", bootstrapConf.urlAccessKey)
                    .replace("##urlSecretKey##", bootstrapConf.urlSecretKey)
                    .replace("##urlEndpoint##", bootstrapConf.urlEndpoint)
                    .replace("##installNode##", bootstrapConf.installNode)
                    .replace("##recipeDownloadMethod##", bootstrapConf.recipeDownloadMethod)

                    .replace("##recipeRelativePath##", bootstrapConf.recipeRelativePath)
                    .replace("##prebootstrapScript##", prebootstrapScript);
            return script;
        } catch (Exception e) {
            throw new RuntimeException("unable to inject bootstrap script",e);
        }
    }


    @Override
    public boolean reboot(ServerNode serverNode) {
        try{
            logger.info("rebooting [{}]", serverNode);
            cloudServerApi.rebuild(serverNode.getNodeId());
            bootstrapMachine(serverNode);
            return true;
        }catch(Exception e){
            // exceptions can happen if cloud does not support this operation. so we regard exceptions as acceptable.
            return false;
        }
    }

    public void init(){
        logger.info("initializing the bootstrapper");
    }

    public void setCloudServerApi(CloudServerApi serverApi) {
        this.cloudServerApi = serverApi;
    }

    public void setBootstrapConf(ServerConfig.BootstrapConfiguration bootstrapConf) {
        this.bootstrapConf = bootstrapConf;
    }

    public ServerConfig.CloudBootstrapConfiguration getCloudBootstrapConf() { return cloudBootstrapConf; }

    public void setCloudBootstrapConf(ServerConfig.CloudBootstrapConfiguration cloudBootstrapConf) { this.cloudBootstrapConf = cloudBootstrapConf; }

    public ScriptExecutor getScriptExecutor() { return scriptExecutor; }

    public void setScriptExecutor(ScriptExecutor scriptExecutor) { this.scriptExecutor = scriptExecutor; }


    public ResourceManagerFactory getResourceManagerFactory() {
        return resourceManagerFactory;
    }

    public void setResourceManagerFactory(ResourceManagerFactory resourceManagerFactory) {
        this.resourceManagerFactory = resourceManagerFactory;
    }
}