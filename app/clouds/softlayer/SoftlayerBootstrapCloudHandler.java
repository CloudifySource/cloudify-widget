package clouds.softlayer;

import java.io.File;
import java.util.Set;

import models.ServerNode;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.jclouds.compute.domain.NodeMetadata;

import play.i18n.Messages;
import play.libs.Json;
import server.ApplicationContext;
import server.ProcExecutor;
import server.exceptions.ServerException;
import utils.CloudifyUtils;
import utils.Utils;
import beans.config.CloudProvider;
import beans.config.Conf;
import clouds.base.AbstractBootstrapCloudHandler;
import clouds.base.AdvancedParams;

public class SoftlayerBootstrapCloudHandler extends AbstractBootstrapCloudHandler {

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

            DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
            ProcExecutor bootstrapExecutor = executorFactory.getBootstrapExecutor( serverNode );

            logger.info( "Executing command line: " + cmdLine );
            bootstrapExecutor.execute( cmdLine, 
            	ApplicationContext.get().conf().server.environment.getEnvironment(), resultHandler );
            logger.info( "waiting for output" );
            resultHandler.waitFor();
            logger.info( "finished waiting , exit value is [{}]", resultHandler.getExitValue() );


            String output = Utils.getOrDefault( Utils.getCachedOutput( serverNode ), "" );
            if ( resultHandler.getException() != null ) {
                logger.info( "we have exceptions, checking for known issues" );
                if ( output.contains( "found existing management machines" ) ) {
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

//            String privateKey = CloudifyUtils.getCloudPrivateKey( cloudFolder );
//            if ( StringUtils.isEmpty( privateKey ) ) {
//                throw new RuntimeException( "Bootstrap failed. No pem file found in cloud directory." );
//            }
            logger.info( "found PEM string" );
            logger.info( "Bootstrap cloud command ended successfully" );

            logger.info( "updating server node with new info" );
            serverNode.setPublicIP( publicIp );
//            serverNode.setPrivateKey( privateKey );

            serverNode.save();
            logger.info("server node updated and saved");
		}catch(Exception e) {
            serverNode.errorEvent("Invalid Credentials").save();
			throw new RuntimeException("Unable to bootstrap cloud", e);
		} 
        finally {
			if (cloudFolder != null && conf.server.cloudBootstrap.removeCloudFolder ) {
				FileUtils.deleteQuietly(cloudFolder);
			}
			if (computeServiceContext != null) {
				computeServiceContext.close();
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