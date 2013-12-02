package clouds.softlayer;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;

import models.ServerNode;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.jclouds.compute.domain.NodeMetadata;

import play.libs.Json;
import server.ApplicationContext;
import beans.config.CloudProvider;
import beans.config.Conf;
import beans.scripts.ScriptExecutor;
import clouds.base.AbstractBootstrapCloudHandler;
import clouds.base.AdvancedParams;

public class SoftlayerBootstrapCloudHandler extends AbstractBootstrapCloudHandler {
	
	@Inject
	private ScriptExecutor scriptExecutor;	

	@Override
	public void createNewMachine( ServerNode serverNode, Conf conf ) {
		
		CloudProvider cloudProvider = getCloudProvider();

		logger.info( "Create Softlayer cloud machine" );
		
		JsonNode parsedParams = Json.parse( serverNode.getAdvancedParams() );
		SoftlayerAdvancedParams params = 
			Json.fromJson( parsedParams.get( PARAMS ), SoftlayerAdvancedParams.class);

        File cloudFolder = null;
        try {
            logger.info( "Creating cloud folder with specific user credentials.  userId: [{}]", params.userId );

            cloudFolder = SoftlayerCloudUtils.createSoftlayerCloudFolder( params.userId, params.apiKey, conf );
            logger.info( "cloud folder is at [{}]", cloudFolder );

            logger.info( "Creating security group for user." );
            ApplicationContext.getCloudifyFactory().createCloudifySecurityGroup( 
            		cloudProvider, getComputeServiceContext( params.userId, params.apiKey ) );

            //Command line for bootstrapping remote cloud.
            CommandLine cmdLine = 
            		new CommandLine( conf.server.cloudBootstrap.remoteBootstrap.getAbsoluteFile() );
            cmdLine.addArgument( cloudFolder.getName() );

            scriptExecutor.runBootstrapScript( 
            		cmdLine, serverNode, null, cloudFolder, conf.server.cloudBootstrap, false );
    	}
    	catch( IOException ioe ){
    		logger.error( ioe.toString(), ioe );
    	}
    	finally {
    		if (cloudFolder != null && conf.server.cloudBootstrap.removeCloudFolder ) {
    			FileUtils.deleteQuietly(cloudFolder);
    		}
    		serverNode.setStopped(true);
    	} 
	}

	@Override
	protected SoftlayerAdvancedParams getAdvancedParameters(ServerNode serverNode) {
    	JsonNode parsedParams = Json.parse( serverNode.getAdvancedParams() );
    	JsonNode paramsJsonNode = parsedParams.get( PARAMS );
    	SoftlayerAdvancedParams params = Json.fromJson( paramsJsonNode, SoftlayerAdvancedParams.class );
    	return params; 
	}
	
	@Override
	public CloudProvider getCloudProvider(){
		return CloudProvider.SOFTLAYER;
	}

	@Override
	protected Set<? extends NodeMetadata> listExistingManagementMachines( AdvancedParams advancedParameters, Conf conf ){
		
		String userId = ( ( SoftlayerAdvancedParams )advancedParameters ).userId;
		String apiKey = ( ( SoftlayerAdvancedParams )advancedParameters ).apiKey;
		
		return getComputeService( userId, apiKey ).
				listNodesDetailsMatching( new MachineNamePrefixPredicate( conf ) );
	}
}