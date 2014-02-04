package clouds.base;


@Deprecated // replaced by another mechanism
abstract public class AbstractBootstrapCloudHandler  {

//	protected static Logger logger = LoggerFactory.getLogger( AbstractBootstrapCloudHandler.class );
//
//    @Inject
//    protected ExecutorFactory executorFactory;
//
//    @Inject
//    protected CloudifyRestClient cloudifyRestClient;
//
//	@Override
//	public ServerNode bootstrapCloud( ServerNode serverNode, Conf conf ) {
//		logger.info( "Bootstrap [" + getCloudProvider().label + "] cloud" );
//
//		serverNode.setRemote( true );
//        // get existing management machine
//		Set<? extends NodeMetadata> existingManagementMachines = null;
//        ComputeServiceContext computeServiceContext = null;
//        try{
//        	AdvancedParams advancedParameters = getAdvancedParameters( serverNode );
//        	computeServiceContext = createComputeServiceContext( advancedParameters );
//
//    		existingManagementMachines = listExistingManagementMachines(
//    						advancedParameters, conf, computeServiceContext.getComputeService() );
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
//        	NodeMetadata managementMachine = CollectionUtils.first( existingManagementMachines );
//            Utils.ServerIp serverIp = Utils.getServerIp( managementMachine );
//
//            if ( !cloudifyRestClient.testRest( serverIp.publicIp ).isSuccess() ){
//                serverNode.errorEvent( "Management machine exists but unreachable" ).save(  );
//                logger.info( "unable to reach management machine on. stopping progress. Testet rest IP [{}]", serverIp );
//                return null;
//            }
//            logger.info( "using first machine  [{}] with ip [{}]", managementMachine, serverIp );
//            serverNode.setServerId( managementMachine.getId() );
//            serverNode.infoEvent("Found management machine on :" + serverIp ).save(  );
//            serverNode.setPublicIP( serverIp.publicIp );
//            serverNode.save();
//            logger.info( "not searching for key - only needed for bootstrap" );
//        }
//        else if( computeServiceContext != null ){
//            logger.info( "did not find an existing management machine, creating new machine" );
//            createNewMachine( serverNode, conf, computeServiceContext );
//        }
//        else{
//        	throw new RuntimeException( "computeServiceContext was not initialized" );
//        }
//        return serverNode;
//	}
//
//	protected ComputeServiceContext createComputeServiceContext( String key, String secretKey ){
//
//
//        return CloudifyUtils.computeServiceContext(getCloudProvider().label, key, secretKey, false);
//
//	}
//
//	abstract protected Set<? extends NodeMetadata> listExistingManagementMachines( AdvancedParams advancedParameters, Conf conf, ComputeService computeService );
//
//	abstract protected AdvancedParams getAdvancedParameters( ServerNode serverNode );
//
//	abstract protected ComputeServiceContext createComputeServiceContext( AdvancedParams advancedParams );
//
//    public static class MachineNamePrefixPredicate implements Predicate<ComputeMetadata>{
//
//        private final String prefix;
//
//        public MachineNamePrefixPredicate( Conf conf ){
//        	prefix = conf.server.cloudBootstrap.existingManagementMachinePrefix;
//        }
//
//        @Override
//        public boolean apply( ComputeMetadata computeMetadata ){
//            // return true if server is not null, prefix is not empty and prefix is prefix of server.getName
//        	boolean startsWithPrefix = computeMetadata.getName().startsWith( prefix );
//        	boolean retValue = !StringUtils.isEmpty( prefix ) && startsWithPrefix;
//        	logger.info( "Within MachineName apply()" +
//        			", computeMetadata.getName()=" + computeMetadata.getName() +
//        			", prefix=" + prefix +
//        			", startsWithPrefix=" + startsWithPrefix +
//        			", retValue=" + retValue );
//            return retValue;
//        }
//
//        @Override
//        public String toString(){
//             return String.format("name has prefix [%s]", prefix);
//        }
//    }
}