/*******************************************************************************
 * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package beans;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.logging.config.NullLoggingModule;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.NovaAsyncApi;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.domain.Server.Status;
import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.jclouds.rest.RestContext;
import org.jclouds.ssh.SshClient;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.jclouds.util.Strings2;

import play.Logger;

import com.google.common.collect.FluentIterable;
import com.google.common.net.HostAndPort;
import com.google.inject.Guice;
import com.google.inject.Injector;

import models.ServerNode;
import server.ApplicationContext;
import server.ServerException;
import server.Utils;

import javax.inject.Inject;

import static server.Config.*;


/**
 * This class manages a compute cloud provider by JCloud openstack nova infrastructure.
 * It provides ability to create/delete specific server with desired flavor configuration.
 * On each new server runs a bootstrap script that prepare machine for a server-pool, 
 * it includes a setup of firewall, JDK, cloudify installation and etc...
 * The bootstrap script can be found under ssh/bootstrap_machine.sh
 * 
 * @author Igor Goldenberg
 */
public class ServerBootstrapper implements server.ServerBootstrapper
{
	private ComputeService _compute;
	private RestContext<NovaApi, NovaAsyncApi> _nova;

    @Inject
    private DeployManager deployManager;
	
    public List<ServerNode> createServers( int numOfServers )
	{
		List<ServerNode> servers = new ArrayList<ServerNode>();

		for( int i=0; i< numOfServers; i++ )
		{
			ServerNode srvNode = null;
	    	try {
				srvNode = createServerNode();
				servers.add( srvNode );
				
				return servers;
			} catch (Exception e) 
			{
				// failed to boostrap machine, nothing todo - let destroy :(
				if ( srvNode != null )
					destroyServer(srvNode.getId());
					
				Logger.error("Failed to bootstrap machine. ", e);
			}
		}
		
		return servers;
	}
	
    
	public void destroyServer( String serverId )
	{
	   deleteServer(serverId);
	}

	
	public ServerBootstrapper()
	{
		ComputeServiceContext context = ContextBuilder.newBuilder(COMPUTE_PROVIDER)
				.credentials(COMPUTE_USERNAME, COMPUTE_APIKEY)
				.buildView(ComputeServiceContext.class);
		_compute = context.getComputeService();
		_nova = context.unwrap();
	}

	
	private ServerNode createServerNode() throws RunNodesException, TimeoutException
	{
		Logger.info(String.format("Starting to create new Server [imageId=%s, flavorId=%s]", COMPUTE_IMAGE_ID, COMPUTE_FLAVOR_ID));

		ServerApi serverApi = _nova.getApi().getServerApiForZone(COMPUTE_ZONE_NAME);
		
		CreateServerOptions serverOpts = new CreateServerOptions();
		serverOpts.keyPairName(COMPUTE_KEY_PAIR);
		serverOpts.securityGroupNames(COMPUTE_SECURITY_GROUP);
		
		ServerCreated serverCreated = serverApi.create(COMPUTE_SERVER_NAME_PREF + System.currentTimeMillis(), COMPUTE_IMAGE_ID, COMPUTE_FLAVOR_ID, serverOpts);
		blockUntilServerInState(serverCreated.getId(), Server.Status.ACTIVE, 1000, 5, serverApi);
		Server server = serverApi.get(serverCreated.getId());

		ServerNode serverNode = new ServerNode( server );
		
		Logger.info("Server created, wait 10 seconds before starting to bootstrap machine: " +  serverNode.getPublicIP() );
		Utils.threadSleep(10000); // need for a network interfaces initialization
		
		// bootstrap machine: firewall, jvm, start cloudify
		bootstrapMachine( serverNode );
		
		Logger.info("Server created. " +  server.getAddresses() );
		
		return serverNode;
	}

	
	public List<Server> getServerList()
	{
		ServerApi serverApi = _nova.getApi().getServerApiForZone(COMPUTE_ZONE_NAME);
		
		FluentIterable<? extends Server> serverIterator = serverApi.listInDetail().concat();   
		
		List<Server> serverList = new ArrayList<Server>();
		for( Server srv : serverIterator )
			serverList.add( srv );

		return serverList;
	}
	
	
	private void deleteServer( String serverId )
	{
		deployManager.destroyExecutor(serverId);
		ServerApi serverApi = _nova.getApi().getServerApiForZone(COMPUTE_ZONE_NAME);
		serverApi.delete(serverId);

		Logger.info("Server id: " + serverId + " was deleted.");
	}
	
	
	/**
	 * Will block until the server is in the correct state.
	 * 
	 * @param serverId The id of the server to block on
	 * @param status The status the server needs to reach before the method stops blocking
	 * @param timeoutSeconds The maximum amount of time to block before throwing a TimeoutException
	 * @param delaySeconds The amount of time between server status checks
	 * @param serverApi The ServerApi used to do the checking
	 * 
	 * @throws TimeoutException If the server does not reach the status by timeoutSeconds
	 */
	private void blockUntilServerInState(String serverId, Status status,
			int timeoutSeconds, int delaySeconds, ServerApi serverApi)
			throws TimeoutException
	{
		int totalSeconds = 0;

		while (totalSeconds < timeoutSeconds)
		{
			Logger.info("Waiting for a server activation... Left timeout: " + (timeoutSeconds - totalSeconds) + " sec");

			Server server = serverApi.get(serverId);

			if (server.getStatus().equals(status))
				return;

			Utils.threadSleep(delaySeconds * 1000);

			totalSeconds += delaySeconds;
		}

		String message = String.format("Timed out at %d seconds waiting for server %s to reach status %s.",
						timeoutSeconds, serverId, status);

		throw new TimeoutException(message);
	}
	
	private void bootstrapMachine( ServerNode server )
	{
		try
		{
			Logger.info("Starting bootstrapping for server: " +  server.getPublicIP() );

			String script = IOUtils.toString(new URL(COMPUTE_BOOTSTRAP_SCRIPT));
			ExecResponse response = runScriptOnNode( server.getPublicIP(), script );
			
			Logger.info("Bootstrap for server: " +  server.getPublicIP() +
					" finished successfully successfully. ExitStatus: " + response.getExitStatus() + 
					"\nOutput: " + response.getOutput() );
		}catch(Exception ex)
		{
			throw new ServerException("Failed to bootstrap cloudify machine: " + server.getPublicIP(), ex);
		}
	}
	
	static public ExecResponse runScriptOnNode(String serverIP, String script)
			throws NumberFormatException, FileNotFoundException, IOException
	{
		Logger.info("Run ssh on server: " + serverIP + " script: " + script);

		Injector i = Guice.createInjector(new SshjSshClientModule(), new NullLoggingModule());
		SshClient.Factory factory = i.getInstance(SshClient.Factory.class);
		SshClient sshConnection = factory.create(HostAndPort.fromParts(serverIP, COMPUTE_SSH_PORT),
				LoginCredentials.builder().user(COMPUTE_SSH_USER)
						.privateKey(Strings2.toStringAndClose(new FileInputStream(COMPUTE_SSH_PRIVATE_KEY))).build());

		ExecResponse execResponse = null;

		try
		{
			sshConnection.connect();
			execResponse = sshConnection.exec(script);
		 }finally 
		 {
			if (sshConnection != null)
			   sshConnection.disconnect();
		 }

		return execResponse;
	}
	
	/**
	 * Always close your service when you're done with it.
	 */
	public void close()
	{
		if (_compute != null)
		{
			_compute.getContext().close();
		}
	}

    public void setDeployManager(DeployManager deployManager) {
        this.deployManager = deployManager;
    }
}