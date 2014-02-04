package cloudify.widget.softlayer;



public class SoftlayerBootstrapCloudHandler{

//extends AbstractBootstrapCloudHandler {
//
//	@Inject
//	private ScriptExecutor scriptExecutor;
//
//	@Override
//	public void createNewMachine( ServerNode serverNode, Conf conf, ComputeServiceContext computeServiceContext ) {
//
//		CloudProvider cloudProvider = getCloudProvider();
//
//		logger.info( "Create Softlayer cloud machine" );
//
//		JsonNode parsedParams = Json.parse( serverNode.getAdvancedParams() );
//		SoftlayerAdvancedParams params =
//			Json.fromJson( parsedParams.get( PARAMS ), SoftlayerAdvancedParams.class);
//
//        File cloudFolder = null;
//        try {
//            logger.info( "Creating cloud folder with specific user credentials.  userId: [{}]", params.userId );
//
//            cloudFolder = SoftlayerCloudUtils.createSoftlayerCloudFolder( params.userId, params.apiKey, conf );
//            logger.info( "cloud folder is at [{}]", cloudFolder );
//
//            logger.info( "Creating security group for user." );
//            ApplicationContext.getCloudifyFactory().createCloudifySecurityGroup(
//            		cloudProvider, computeServiceContext );
//
//            //Command line for bootstrapping remote cloud.
//            CommandLine cmdLine =
//            		new CommandLine( conf.server.cloudBootstrap.remoteBootstrap.getAbsoluteFile() );
//            cmdLine.addArgument( cloudFolder.getName() );
//
//            scriptExecutor.runBootstrapScript( cmdLine, serverNode, null, cloudFolder, false );
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
//	@Override
//	protected SoftlayerAdvancedParams getAdvancedParameters(ServerNode serverNode) {
//    	JsonNode parsedParams = Json.parse( serverNode.getAdvancedParams() );
//    	JsonNode paramsJsonNode = parsedParams.get( PARAMS );
//    	SoftlayerAdvancedParams params = Json.fromJson( paramsJsonNode, SoftlayerAdvancedParams.class );
//    	return params;
//	}
//
//	@Override
//	public CloudProvider getCloudProvider(){
//		return CloudProvider.SOFTLAYER;
//	}
//
//	@Override
//	protected Set<? extends NodeMetadata> listExistingManagementMachines( AdvancedParams advancedParameters, Conf conf, ComputeService computeService ){
//
//		return computeService.listNodesDetailsMatching( new MachineNamePrefixPredicate( conf ) );
//	}
//
//	@Override
//	protected ComputeServiceContext createComputeServiceContext( AdvancedParams advancedParams ){
//
//	    String userId = ( ( SoftlayerAdvancedParams )advancedParams ).userId;
//	    String apiKey = ( ( SoftlayerAdvancedParams )advancedParams ).apiKey;
//		return createComputeServiceContext( userId, apiKey );
//	}
	
}