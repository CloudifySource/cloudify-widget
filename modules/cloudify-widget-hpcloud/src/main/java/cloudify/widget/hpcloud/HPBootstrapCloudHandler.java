package cloudify.widget.hpcloud;


public class HPBootstrapCloudHandler{}
//implements BootstrapCloudHandler {
//
//    @Inject
//    private CloudifyRestClient cloudifyRestClient;
//
//	@Inject
//	private ScriptExecutor scriptExecutor;
//
//    private static Logger logger = LoggerFactory.getLogger( HPBootstrapCloudHandler.class );
//
//    @Override
//    public void createNewMachine(ServerNode serverNode, Conf conf, ComputeServiceContext computeServiceContext) {
//
//    	CloudProvider cloudProvider = getCloudProvider();
//
//    	logger.info( "Create HP cloud machine" );
//
//    	File cloudFolder = null;
//    	ComputeServiceContext jCloudsContext = null;
//    	try {
//    		HPAdvancedParams params = getHPAdvancedParameters( serverNode );
//    		String project = params.getProject();
//    		String secretKey = params.getSecretKey();
//    		String apiKey = params.getKey();
//
//    		logger.info( "Creating cloud folder with specific user credentials. " +
//    				"Project: [{}], api key: [{}]", project, apiKey );
//    		jCloudsContext = CloudifyUtils.createJcloudsContext( project, apiKey, secretKey );
//    		cloudFolder = ApplicationContext.getCloudifyFactory().
//    				createCloudFolder( cloudProvider, project, apiKey, secretKey, jCloudsContext );
//    		logger.info( "cloud folder is at [{}]", cloudFolder );
//
//    		logger.info( "Creating security group for user." );
//    		ApplicationContext.getCloudifyFactory().
//    							createCloudifySecurityGroup( cloudProvider, jCloudsContext );
//
//    		//Command line for bootstrapping remote cloud.
//    		CommandLine cmdLine = new CommandLine( conf.server.cloudBootstrap.remoteBootstrap.getAbsoluteFile() );
//    		cmdLine.addArgument( cloudFolder.getName() );
//
//    		scriptExecutor.runBootstrapScript( cmdLine, serverNode, jCloudsContext, cloudFolder, true );
//    	}
//    	catch( IOException ioe ){
//    		logger.error( ioe.toString(), ioe );
//    	}
//    	finally {
//    		if (cloudFolder != null && conf.server.cloudBootstrap.removeCloudFolder ) {
//    			FileUtils.deleteQuietly(cloudFolder);
//    		}
//    		serverNode.setStopped(true);
//    	}
//	}
//
//
//	@Override
//	public ServerNode bootstrapCloud(ServerNode serverNode, Conf conf) {
//
//		logger.info( "Bootstrap HP cloud" );
//
//		serverNode.setRemote( true );
//        // get existing management machine
//        List<CloudServer> existingManagementMachines = null;
//        try{
//
//        	HPAdvancedParams params = getHPAdvancedParameters( serverNode );
//    		String project = params.getProject();
//    		String key = params.getKey();
//    		String secretKey = params.getSecretKey();
//
//            existingManagementMachines =
//            		CloudifyUtils.getAllMachinesWithPredicate( new ServerNamePrefixPredicate(conf),
//            		new NovaContext( conf.server.cloudProvider,
//            				project, key, secretKey,
//            				conf.server.cloudBootstrap.zoneName, true ) );
//        }
//        catch(Exception e){
//            if ( ExceptionUtils.indexOfThrowable( e, AuthorizationException.class ) > 0 ){
//                serverNode.errorEvent( "Invalid Credentials" ).save(  );
//                return null;
//            }
//            logger.error( "unrecognized exception, assuming only existing algorithm failed. ",e );
//        }
//
//        logger.info( "found [{}] management machines", CollectionUtils.size( existingManagementMachines ) );
//
//        if ( !CollectionUtils.isEmpty( existingManagementMachines ) ) {
//
//        	CloudServer managementMachine = CollectionUtils.first( existingManagementMachines );
//
//            // GUY - for some reason
//
//            // ((Address )managementMachine.getAddresses().get("private").toArray()[1]).getAddr()
//
//            Utils.ServerIp serverIp = Utils.getServerIp( managementMachine );
//
//            if ( !cloudifyRestClient.testRest( serverIp.publicIp ).isSuccess() ){
//                serverNode.errorEvent( "Management machine exists but unreachable" ).save(  );
//                logger.info( "unable to reach management machine. stopping progress." );
//                return null;
//            }
//            logger.info( "using first machine  [{}] with ip [{}]", managementMachine, serverIp );
//            serverNode.setServerId( managementMachine.getId() );
//            serverNode.infoEvent("Found management machine on :" + serverIp ).save(  );
//            serverNode.setPublicIP( serverIp.publicIp );
//            serverNode.save(  );
//            logger.info( "not searching for key - only needed for bootstrap" );
//        }
//        else {
//            logger.info( "did not find an existing management machine, creating new machine" );
//            createNewMachine( serverNode, conf, null );
//        }
//
//        return serverNode;
//	}
//
//	private static HPAdvancedParams getHPAdvancedParameters( ServerNode serverNode ) {
//    	JsonNode parsedParams = Json.parse( serverNode.getAdvancedParams() );
//    	HPAdvancedParams params = Json.fromJson( parsedParams.get( PARAMS ), HPAdvancedParams.class );
//    	return params;
//	}
//
//    class ServerNamePrefixPredicate implements Predicate<CloudServer>{
//
//        private final String prefix;
//
//        ServerNamePrefixPredicate( Conf conf ){
//        	this.prefix = conf.server.cloudBootstrap.existingManagementMachinePrefix;
//        }
//
//        @Override
//        public boolean apply( CloudServer server ){
//            // return true iff server is not null, prefix is not empty and prefix is prefix of server.getName
//            return server != null && !StringUtils.isEmpty( prefix ) && server.getName().indexOf( prefix ) == 0;
//        }
//
//        @Override
//        public String toString(){
//             return String.format("name has prefix [%s]", prefix);
//        }
//    }
//
//
//	@Override
//	public CloudProvider getCloudProvider() {
//		// TODO Auto-generated method stub
//		return CloudProvider.HP;
//	}
//}