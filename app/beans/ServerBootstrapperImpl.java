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

import java.util.*;


import cloudify.widget.api.clouds.CloudServer;
import models.ServerNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.ServerBootstrapper;
import server.exceptions.BootstrapException;

/**
 * This class manages a compute cloud provider by JCloud openstack nova infrastructure.
 * It provides ability to create/delete specific server with desired flavor configuration.
 * On each new server runs a bootstrap script that prepare machine for a server-pool,
 * it includes a setup of firewall, JDK, cloudify installation and etc...
 * The bootstrap script can be found under ssh/bootstrap_machine.sh
 *
 *
 * @author Igor Goldenberg
 */
public class ServerBootstrapperImpl implements ServerBootstrapper
{

    private static Logger logger = LoggerFactory.getLogger( ServerBootstrapperImpl.class );

    @Override
    public List<ServerNode> createServers(int numOfServers) {
        logger.info("creating [{}] new server", numOfServers);
        return null;
    }

    @Override
    public void destroyServer(ServerNode serverNode) {
        logger.info("destroying server [{}]", serverNode);
    }

    @Override
    public void deleteServer(String nodeId) {
        logger.info("destroying server [{}]", nodeId);
    }

    @Override
    public BootstrapValidationResult validateBootstrap(ServerNode serverNode) {
        logger.info("validating bootstrap on [{}]", serverNode );
        return null;
    }

    @Override
    public void close() {
       logger.info("closing");
    }

    @Override
    public ServerNode bootstrapCloud(ServerNode serverNode) throws BootstrapException {
        logger.info("bootstrapping cloud with details [{}]", serverNode);
        return null;
    }

    @Override
    public List<ServerNode> recoverUnmonitoredMachines() {
        logger.info("recovering lost machines");
        return null;
    }

    @Override
    public Collection<CloudServer> getAllMachines(NovaCloudCredentials cloudCredentials) {
        logger.info("getting all machines for credentials [{}]", cloudCredentials);
        return null;
    }

    @Override
    public boolean reboot(ServerNode serverNode) {
        logger.info("rebooting [{}]", serverNode);
        return false;
    }

    //
//	private NovaContext novaContext;
//
//    private int retries = 2;
//    private int bootstrapRetries = 2;
//
//    @Inject
//    private Conf conf;
//
//    @Inject
//    private ExecutorFactory executorFactory;
//
//    @Inject
//    private PoolEventListener poolEventManager;
//
//    // this is an incrementing ID starting from currentMilliTime..
//    // resolves issue where we got "Server by this name already exists"
//    private AtomicLong incNodeId = new AtomicLong(  System.currentTimeMillis() );
//
//    @Inject
//    private DeployManager deployManager;
//
//    @Inject
//    private CloudifyRestClient cloudifyRestClient;
//
//    private CloudProvider cloudProvider;
//
//    public List<ServerNode> createServers( int numOfServers )
//	{
//		List<ServerNode> servers = new ArrayList<ServerNode>();
//		logger.info("creating {} new instances", numOfServers );
//		for( int i=0; i< numOfServers; i++ )
//		{
//            ServerNode server = null;
//			try {
//				server = createServer();
//			}
//			catch (RunNodesException e) {
//				logger.error( "Falied to created server, " + e.toString(), e );
//			}
//            if ( server != null ){
//                poolEventManager.handleEvent(new PoolEvent.ServerNodeEvent().setServerNode(server).setType(PoolEvent.Type.CREATE));
//                servers.add( server );
//            }
//        }
//		return servers;
//	}
//
//    public CloudServerApi getCloudDriver(){
//        //TODO : implement this
//        return null;
//    }
//
//
//    /**
//     * This method will try to create a server.
//     * Creating a server includes 2 steps :
//     * 1. Getting a machine up and running
//     * 2. Installing Cloudify on the machine.
//     *
//     * If any of the above fails, it is the "createServer" responsibility Applicationto clean the workspace.
//     * In this case - the method will return NULL.
//     *
//     * @return ServerNode if creation was successful.
//     */
//    public ServerNode createServer() throws RunNodesException
//    {
//
//        ServerNode serverNode = null;
//        int i =0;
//        for ( ; i < retries && serverNode == null ; i++){
//            logger.info( "creating new server node, try #[{}]",i );
//            ServerNode tmpNode = createMachine();
//            PoolEvent.ServerNodeEvent newServerNodeEvent = new PoolEvent.ServerNodeEvent().setType(PoolEvent.Type.CREATE).setServerNode(tmpNode);
//            if (tmpNode != null) {
//                if (bootstrap(tmpNode)) { // bootstrap success
//                    poolEventManager.handleEvent(newServerNodeEvent);
//                    serverNode = tmpNode;
//                    logger.info("successful bootstrap on [{}]", serverNode);
//                } else { // bootstrap failed
//                    logger.info("bootstrap failed, deleting server");
//                    deleteServer(tmpNode.getNodeId()); // deleting the machine from cloud.
//                }
//            } else { // create server failed
//                logger.info("unable to create machine. try [{}/{}]", i + 1, retries);
//            }
//
//        }
//        return serverNode;
//
//    }
//
//
//    // guy - todo - need to ping machine first. If machine is down, we cannot validate - throw exception.
//    // guy - todo - if machine is up we do the same thing we do today.
//    @Override
//    public BootstrapValidationResult validateBootstrap( ServerNode serverNode )
//    {
//        BootstrapValidationResult result = new BootstrapValidationResult();
//        try{
//            result.machineReachable = isMachineReachable( serverNode );
//        }catch(Exception e){
//            result.machineReachableException = e;
//        }
//
//        if ( result.machineReachable == Boolean.TRUE ){
//            try{
//                result.managementVersion = cloudifyRestClient.getVersion( serverNode.getPublicIP() ).getVersion();
//                result.managementAvailable = true;
//            }catch( Exception e ){
//                logger.debug( "got exception while checking management version",e );
//                result.managementAvailable = false;
//            }
//        }
//        return result;
//    }
//
//    private boolean isMachineReachable( ServerNode serverNode ) throws Exception
//    {
////        logger.info( "pinging machine [{}]", serverNode );
////        String publicIP = serverNode.getPublicIP();
////        InetAddress byName = InetAddress.getByName( publicIP );
////
////        boolean reachable = byName.isReachable( 5000 );
////        logger.info( "machine is reachable [{}]", reachable );
////        return reachable;
//        return true; // guy - there's a problem pinging machines.
//    }
//
//
//	public void destroyServer( ServerNode serverNode)
//	{
//       logger.info("destroying server {}", serverNode );
//        if ( !StringUtils.isEmpty( serverNode.getNodeId() ) ){
//            deleteServer( serverNode.getNodeId() );
//        }
//        if ( serverNode.getId() != null ){
//            logger.info("deleting from DB");
//            serverNode.refresh();
//            serverNode.delete(  );
//        }else{
//            logger.info("server node saved in database, nothing to delete");
//        }
//	}
//
//    @Override
//    public Collection<CloudServer> getAllMachines(NovaCloudCredentials cloudCredentials){
//        return getAllMachinesWithTag(new NovaContext(cloudCredentials));
//    }
//
//    /**
//     * We encourage using conf.server.bootstrap.tags
//     * This will tag all created machines.
//     *
//     * For example - use tag "managed-by-my-cloudify-widget-instance"
//     *
//     * Use a different confTags for each server instance you have - so you can tell which machine is in which pool.
//       This function in turn should get all machines in the pool.
//     * @return - all machines that contain all these tags.
//     */
//    public Collection<CloudServer> getAllMachinesWithTag( NovaContext context){
//        String confTag =  conf.server.bootstrap.tag;
//        logger.info( "getting all machines with tag [{}]", confTag );
//
//        if ( StringUtils.isEmpty( confTag ) ){
//            logger.info( "confTags is null, not finding all machines" );
//            return new LinkedList<CloudServer>();
//        }
//
//        // get all servers with tags matching my configuration.
//        return getCloudDriver().getAllMachinesWithTag( confTag );
//    }
//
//	class TruePredicate implements Predicate<CloudServer>{        @Override
//        public boolean apply(CloudServer server) {
//            return server != null;
//        }
//
//        @Override
//        public String toString(){
//            return "always true predicate";
//        }
//    }
//
//    class ServerTagPredicate implements Predicate<CloudServer> {
//
//        String confTags =  conf.server.bootstrap.tag;
//        List<String> confTagsList = null;
//
//        public ServerTagPredicate(){
//            if ( !StringUtils.isEmpty( confTags )){
//                confTagsList = Arrays.asList( StringUtils.stripAll( confTags.split( "," ) ) );
//            }
//        }
//
//        @Override
//        public boolean apply( CloudServer server )
//        {
//            if ( server == null ) {
//                return false;
//            }
//
//            Map<String, String> metadata = server.getMetadata();
//            if ( !CollectionUtils.isEmpty( metadata ) && metadata.containsKey( "tags" ) ) {
//                String tags = metadata.get( "tags" );
//                if ( !StringUtils.isEmpty( tags ) && !CollectionUtils.isEmpty( confTagsList ) ) {
//                    logger.info( "comparing tags [{}] with confTags [{}]", tags, confTags );
//                    List<String> tagList = Arrays.asList( StringUtils.stripAll( tags.split( "," ) ) );
//                    return CollectionUtils.isSubCollection( confTagsList, tagList );
//                }
//            }
//            return false;
//        }
//
//        @Override
//        public String toString()
//        {
//            return String.format("has tags [%s]",confTags);
//        }
//    }
//
//    @Override
//    public List<ServerNode> recoverUnmonitoredMachines(){
//        List<ServerNode> result = new ArrayList<ServerNode>(  );
//        logger.info( "recovering all list machines" );
//        Collection<CloudServer> allMachinesWithTag = getAllMachinesWithTag( novaContext );
//        logger.info( "found [{}] total machines with matching tags filtering lost", CollectionUtils.size( allMachinesWithTag )  );
//        if ( !CollectionUtils.isEmpty( allMachinesWithTag )){
//            for ( CloudServer server : allMachinesWithTag ) {
//                ServerNode serverNode = ServerNode.getServerNode( server.getId() );
//                if ( serverNode == null ){
//                    ServerNode newServerNode = new ServerNode( server );
//                    logger.info( "found an unmonitored machine - I should add it to the DB [{}]", newServerNode );
//                    result.add( newServerNode );
//                }
//            }
//        }
//        return result;
//    }
//
//    public void init() {
//        try {
//            novaContext = new NovaContext(conf.server.cloudProvider, conf.server.bootstrap.api.project, conf.server.bootstrap.api.key, conf.server.bootstrap.api.secretKey, conf.server.bootstrap.zoneName, false);
//            cloudProvider = novaContext.cloudProvider;
//        } catch (RuntimeException e) {
//            logger.error("unable to initialize server bootstrapper", e);
//            throw e;
//        }
//    }
//
//    public static class NovaContext{
//        final ComputeServiceContext context;
//        final String zone;
//
//        CloudServerApi api = null;
////        RestContext<NovaApi, NovaAsyncApi> nova = null;
////        RestContext cloud = null;
//        Object cloudApi;
//        ComputeService computeService = null;
//        final CloudProvider cloudProvider;

		

//        public NovaContext(NovaCloudCredentials cloudCredentials) {
//            logger.info("initializing bootstrapper with cloudCredentials [{}]", cloudCredentials.toString());
//            Properties overrides = new Properties();
//            if (cloudCredentials.apiCredentials) {
//                overrides.put("jclouds.keystone.credential-type", "apiAccessKeyCredentials");
//            }
//            cloudProvider = cloudCredentials.cloudProvider;
//            context = CloudifyUtils.computeServiceContext( cloudCredentials.cloudProvider.label ,cloudCredentials.getIdentity(), cloudCredentials.getCredential(), true );
//
//
//            this.zone = cloudCredentials.zone;
//        }

//        public NovaContext( CloudProvider cloudProvider, String project, String key, String secretKey, String zone, boolean apiCredentials )
//        {
//            // todo : ugly - we should resort to "credentials factory" - will be required once we support other platforms other than Nova.
//             this(ApplicationContext.getNovaCloudCredentials()
//                     .setCloudProvider(cloudProvider)
//                     .setProject(project)
//                     .setKey(key)
//                     .setApiCredentials(apiCredentials)
//                     .setZone(zone)
//                     .setSecretKey(secretKey));
//        }

        /*
        private RestContext getCloud(){
            if ( cloud == null ){
            	TypeToken<?> backendType = context.getBackendType();
            	cloud = context.unwrap();
            }
            return cloud;
        }*/

//        private Object getCloudApi(){
//        	if( cloudApi == null ){
////        		cloudApi = CloudifyFactory.createCloudApi( getComputeService(), cloudProvider, getCloudApi() );
//
//        		RestContext restContext = context.unwrap();
//        		cloudApi = restContext.getApi();
////        		cloudApi = contextBuilder.buildApi(
////        				(Class)( ( RestApiMetadata )contextBuilder.getApiMetadata() ).getApi() );
//        	}
//
//        	return cloudApi;
//        }
        
//        public CloudServerApi getApi(){
//            if( api == null ){
////            	RestContext cloudRestContext = getCloud();
////            	Object cloudRestContextApi = cloudRestContext.getApi();
//            	CloudApi cloudApiLocal = ApplicationContext.getCloudifyFactory().
//            					createCloudApi( getComputeService(), cloudProvider, getCloudApi() );
//            	CloudServerApi serverApiForZone = cloudApiLocal.getServerApiForZone( zone );
//                api = serverApiForZone;
//            }
//            return api;
//        }
//        public ComputeService getComputeService(){
//            if ( computeService == null ){
//                computeService = context.getComputeService();
//            }
//            return computeService;
//        }
//
//        public void close()
//        {
//            context.close();
//        }
//    }

//    private ServerNode createMachine() throws RunNodesException{
//        logger.info( "Starting to create new Server [imageId={}, flavorId={}]", conf.server.bootstrap.imageId, conf.server.bootstrap.flavorId );
//
//        final CloudServerApi serverApi = novaContext.getApi();
////        CreateServerOptions serverOptions = new CreateServerOptions();
//
////        Map<String,String> metadata = new HashMap<String, String>();
////
////        List<String> tags = new LinkedList<String>();
////
////        if ( !StringUtils.isEmpty(conf.server.bootstrap.tags) ){
////            tags.add( conf.server.bootstrap.tags );
////        }
////
////        metadata.put("tags", StringUtils.join(tags, ","));
////        serverOptions.metadata(metadata);
////        serverOptions.keyPairName( conf.server.bootstrap.keyPair );
////        serverOptions.securityGroupNames(conf.server.bootstrap.securityGroup);
//
//        CloudCreateServerOptions serverOpts = ApplicationContext.getCloudifyFactory().
//        									createCloudCreateServerOptions( cloudProvider, conf );
//
//
//        ComputeService computeService = novaContext.getComputeService();
//
//
//        TemplateOptions templateOptions = new TemplateOptions();
//
//
////        final ServerCreated serverCreated = serverApi.create(
////        		conf.server.bootstrap.serverNamePrefix + incNodeId.incrementAndGet(),
////        		conf.server.bootstrap.imageId , conf.server.bootstrap.flavorId, serverOpts);
//
//        final CloudServerCreated serverCreated = serverApi.create(
//        		conf.server.bootstrap.serverNamePrefix + incNodeId.incrementAndGet(),
//        		conf.server.bootstrap.imageId, conf.server.bootstrap.flavorId, serverOpts );
//
//        logger.info("waiting for serverId activation [{}]", serverCreated.getId());
//        // start the event
//        PoolEvent.MachineStateEvent poolEvent = new PoolEvent.MachineStateEvent().setType(PoolEvent.Type.CREATE)/*.setResource(serverCreated)TODO?*/;
//        poolEventManager.handleEvent(poolEvent);
//        final ActiveWait wait = new ActiveWait();
//        if ( wait
//                .setIntervalMillis(TimeUnit.SECONDS.toMillis(5))
//                .setTimeoutMillis(TimeUnit.SECONDS.toMillis(120))
//                .waitUntil(new Wait.Test() {
//                    @Override
//                    public boolean resolved() {
//                        logger.info("Waiting for a server activation... Left timeout: {} sec", wait.getTimeLeftMillis() / 1000);
//                        return serverApi.get(serverCreated.getId()).getStatus().equals(CloudServerStatus.ACTIVE);
//                    }
//                }))
//        {
//        	CloudServer server = serverApi.get( serverCreated.getId());
//            poolEventManager.handleEvent(poolEvent./*setResource(server).TODO?*/setType(PoolEvent.Type.UPDATE));
//            logger.info("Server created.{} ", server.getAddresses());
//            return new ServerNode( server );
//        }
//
//        logger.info("server did not become active.");
//        return null;
//
//    }


//	@Override
//    public boolean reboot(ServerNode serverNode){
//        rebuild( serverNode);
//        return bootstrap( serverNode );
//    }
//
//    private void rebuild( ServerNode serverNode ){
//        logger.info("rebuilding machine");
//        CloudServerApi serverApi = novaContext.getApi();
//        try {
//            serverApi.rebuild( serverNode.getNodeId() );
//        } catch (RuntimeException e) {
//            logger.error("error while rebuilding machine [{}]", serverNode, e);
//        }
//    }

//    private boolean bootstrap( ServerNode serverNode ){
//        long timeout = conf.server.bootstrap.sleepBeforeBootstrapMillis;
//        logger.info("Server created, wait {} seconds before starting to bootstrap machine: {}", timeout, serverNode.getPublicIP());
//        Utils.threadSleep(timeout); // need for a network interfaces initialization
//
//
//        boolean bootstrapSuccess = false;
//        Exception lastBootstrapException = null;
//        for (int i = 0; i < bootstrapRetries && !bootstrapSuccess; i++) {
//            // bootstrap machine: firewall, jvm, start cloudify
//            logger.info("bootstrapping machine try #[{}]", i);
//            try {
//                bootstrapMachine(serverNode);
//                BootstrapValidationResult bootstrapValidationResult = validateBootstrap(serverNode);
//                if (bootstrapValidationResult.isValid()) {
//                    bootstrapSuccess = true;
//                } else {
//                    logger.info("machine [{}] did not bootstrap successfully [{}] retrying", serverNode, bootstrapValidationResult);
//                    rebuild( serverNode );
//                }
//            } catch (RuntimeException e) {
//                lastBootstrapException = e;
//            }
//        }
//
//        if (!bootstrapSuccess) {
////            poolEventManager.handleEvent(new PoolEvent.ServerNodeEvent()
////                    .setType(PoolEvent.Type.UPDATE)
////                    .setServerNode(serverNode)
////                    .setErrorMessage(lastBootstrapException.getMessage())
////                    .setErrorStackTrace(ExceptionUtils.getFullStackTrace(lastBootstrapException)));
////            logger.error("unable to bootstrap machine", lastBootstrapException);
//        }
//        return bootstrapSuccess;
//
//    }
//
//    @Override
//    public ServerNode bootstrapCloud( ServerNode serverNode )
//    {
//    	BootstrapCloudHandler bootstrapCloudHandler =
//    			ApplicationContext.getBootstrapCloudHandler( cloudProvider );
//    	return bootstrapCloudHandler.bootstrapCloud( serverNode, conf );
//    }

/*
    private ComputeServiceContext createComputeServiceContext( 
    		CloudProvider cloudProvider, String key, String secretKey ){

    	Properties overrides = new Properties();
    	overrides.setProperty( 
    			"jclouds.timeouts.AccountClient.getActivePackages", String.valueOf( 10*60*1000 ) );

    	ContextBuilder contextBuilder = ContextBuilder.newBuilder( cloudProvider.label );
    	return contextBuilder 
    			.credentials( key, secretKey )
    			.overrides( overrides )
    			.buildView( ComputeServiceContext.class );
    }    
    */
    /*
    
    private void createNewMachine( ServerNode serverNode )
    {
        File cloudFolder = null;
        ComputeServiceContext jCloudsContext = null;
        try {
        	JsValue parse = Json.parse( serverNode.getAdvancedParams() );
        	
            // no existing management machine - create new server
            String project = serverNode.getProject();
            String secretKey = serverNode.getSecretKey();
            String apiKey = serverNode.getKey();
            logger.info( "Creating cloud folder with specific user credentials. Project: [{}], api key: [{}]", project, apiKey );
            jCloudsContext = CloudifyUtils.createJcloudsContext( project, apiKey, secretKey );
            cloudFolder = ApplicationContext.getCloudifyFactory().
            		createCloudFolder( cloudProvider, project, apiKey, secretKey, jCloudsContext );
            logger.info( "cloud folder is at [{}]", cloudFolder );

            logger.info( "Creating security group for user." );
            ApplicationContext.getCloudifyFactory().
            						createCloudifySecurityGroup( cloudProvider, jCloudsContext );

            //Command line for bootstrapping remote cloud.
            CommandLine cmdLine = new CommandLine( conf.server.cloudBootstrap.remoteBootstrap.getAbsoluteFile() );
            cmdLine.addArgument( cloudFolder.getName() );

            DefaultExecuteResultHandler resultHandler = executorFactory.getResultHandler(cmdLine.toString());
            ProcExecutor bootstrapExecutor = executorFactory.getBootstrapExecutor( serverNode );

            logger.info("Executing command line: " + cmdLine);
            bootstrapExecutor.execute(cmdLine, ApplicationContext.get().conf().server.environment.getEnvironment(), resultHandler);
            logger.info("waiting for output");
            resultHandler.waitFor();
            logger.info("finished waiting , exit value is [{}]", resultHandler.getExitValue());


            String output = Utils.getOrDefault( Utils.getCachedOutput( serverNode ), "" );
            if ( resultHandler.getException() != null ) {
                logger.info( "we have exceptions, checking for known issues" );
                if ( output.contains( "Found existing servers matching the name" ) ) {
                    logger.info( "found 'found existing management machines' - issuing cloudify already exists message" );
                    throw new ServerException( Messages.get( "cloudify.already.exists" ) );
                }
                logger.info( "Command execution ended with errors: {}", output );
                throw new RuntimeException( "Failed to bootstrap cloudify machine: "
                        + output, resultHandler.getException() );
            }

            logger.info( "finished handling errors, extracting IP" );
            String publicIp = Utils.extractIpFromBootstrapOutput( output );
            if ( StringUtils.isEmpty( publicIp ) ) {
                logger.warn( "No public ip address found in bootstrap output. " + output );
                throw new RuntimeException( "Bootstrap failed. No IP address found in bootstrap output."
                        + output, resultHandler.getException() );
            }
            logger.info( "ip is [{}], saving to serverNode", publicIp );

            String privateKey = CloudifyUtils.getCloudPrivateKey( cloudFolder );
            if ( StringUtils.isEmpty( privateKey ) ) {
                throw new RuntimeException( "Bootstrap failed. No pem file found in cloud directory." );
            }
            logger.info( "found PEM string" );
            logger.info( "Bootstrap cloud command ended successfully" );

            logger.info( "updating server node with new info" );
            serverNode.setPublicIP( publicIp );
            serverNode.setPrivateKey( privateKey );

            serverNode.save();
            logger.info("server node updated and saved");
		}catch(Exception e) {
            serverNode.errorEvent("Invalid Credentials").save();
			throw new RuntimeException("Unable to bootstrap cloud", e);
		} finally {
			if (cloudFolder != null && conf.server.cloudBootstrap.removeCloudFolder ) {
				FileUtils.deleteQuietly(cloudFolder);
			}
			if (jCloudsContext != null) {
				jCloudsContext.close();
			}
			serverNode.setStopped(true);
			
		}
	}
    
    */

//	public void deleteServer( String serverId )
//	{
//        try{
//        	CloudServerApi api = novaContext.getApi();
//            CloudServer server = api.get( serverId );
//            if ( server != null ){
//                api.delete(serverId);
//                server = api.get( serverId );
//		        logger.info("Server id: {} was terminated.", serverId);
//            }
//            poolEventManager.handleEvent(new PoolEvent.MachineStateEvent().setType(PoolEvent.Type.DELETE)/*.setResource(server)TODO?*/);
//        }catch(Exception e){
//            logger.error("unable to delete server [{}]", serverId);
//        }
//
//	}




//	private void bootstrapMachine( ServerNode server )
//	{
//		try
//		{
//			logger.info("Starting bootstrapping for server:{} " , server );
//
//			String script = FileUtils.readFileToString( conf.server.bootstrap.script );
//            script = script.replace("##publicip##", server.getPublicIP()).replace("##privateip##", server.getPrivateIP());
//
//			ExecResponse response = runScriptOnNode( conf, server.getPublicIP(), script );
//
//            logger.info("script finished");
//			logger.info("Bootstrap for server: {} finished successfully successfully. " +
//                    "ExitStatus: {} \nOutput:  {}", new Object[]{server,
//                    response.getExitStatus(),
//                    response.getOutput()} );
//		}catch(Exception ex)
//		{
//            logger.error("unable to bootstrap machine [{}]", server, ex);
//            try{
//                destroyServer( server );
//            }catch(Exception e){
//                logger.info("destroying server after failed bootstrap threw exception",e);
//            }
//			throw new ServerException("Failed to bootstrap cloudify machine: " + server.toDebugString(), ex);
//		}
//	}
//
//	static public ExecResponse runScriptOnNode( Conf conf, String serverIP, String script)
//			throws NumberFormatException, IOException
//	{
//		logger.debug("Run ssh on server: {} script: {}" , serverIP, script );
//        Injector i = Guice.createInjector(new SshjSshClientModule(), new NullLoggingModule());
//		SshClient.Factory factory = i.getInstance(SshClient.Factory.class);
//		SshClient sshConnection = factory.create(HostAndPort.fromParts(serverIP, conf.server.bootstrap.ssh.port ),
//				LoginCredentials.builder().user( conf.server.bootstrap.ssh.user )
//						.privateKey(Strings2.toStringAndClose(new FileInputStream( conf.server.bootstrap.ssh.privateKey ))).build());
//        ExecResponse execResponse = null;
//		try
//		{
//			sshConnection.connect();
//            logger.info("ssh connected, executing");
//			execResponse = sshConnection.exec(script);
//            logger.info("finished execution");
//		 }finally
//		 {
//			if (sshConnection != null)
//			   sshConnection.disconnect();
//		 }
//
//		return execResponse;
//	}
//
//	/**
//	 * Always close your service when you're done with it.
//	 */
//	public void close()
//	{
//         if ( novaContext != null)
//		{
//			novaContext.close();
//		}
//	}
//
//    public void setDeployManager(DeployManager deployManager) {
//        this.deployManager = deployManager;
//    }
//
//    public void setConf( Conf conf )
//    {
//        this.conf = conf;
//    }
//
//    public void setRetries( int retries )
//    {
//        this.retries = retries;
//    }
//
//    public void setBootstrapRetries( int bootstrapRetries )
//    {
//        this.bootstrapRetries = bootstrapRetries;
//    }
//
//    public void setCloudifyRestClient( CloudifyRestClient cloudifyRestClient )
//    {
//        this.cloudifyRestClient = cloudifyRestClient;
//    }


}