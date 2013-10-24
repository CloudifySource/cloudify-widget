package clouds.hp;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import models.ServerNode;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.rest.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.i18n.Messages;
import play.libs.Json;
import server.ApplicationContext;
import server.ProcExecutor;
import server.exceptions.ServerException;
import utils.CloudifyUtils;
import utils.CollectionUtils;
import utils.Utils;
import beans.ServerBootstrapperImpl.NovaContext;
import beans.api.ExecutorFactory;
import beans.cloudify.CloudifyRestClient;
import beans.config.CloudProvider;
import beans.config.Conf;
import clouds.base.BootstrapCloudHandler;
import clouds.base.CloudServer;

import com.google.common.base.Predicate;

public class HPBootstrapCloudHandler implements BootstrapCloudHandler {

    @Inject
    private ExecutorFactory executorFactory;
    
    @Inject
    private CloudifyRestClient cloudifyRestClient;    

    private static Logger logger = LoggerFactory.getLogger( HPBootstrapCloudHandler.class );
	
	
	@Override
	public void createNewMachine(ServerNode serverNode, Conf conf) {

		CloudProvider cloudProvider = getCloudProvider();
		
		logger.info( "Create HP cloud machine" );
		
        File cloudFolder = null;
        ComputeServiceContext jCloudsContext = null;
        try {
        	HPAdvancedParams params = getHPAdnacedParameters( serverNode );
        	String project = params.getProject();
            String secretKey = params.getSecretKey();
            String apiKey = params.getKey();

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

            DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
            ProcExecutor bootstrapExecutor = executorFactory.getBootstrapExecutor( serverNode );

            logger.info( "Executing command line: " + cmdLine );
            bootstrapExecutor.execute( cmdLine, ApplicationContext.get().conf().server.environment.getEnvironment(), resultHandler );
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
		} 
        finally {
			if (cloudFolder != null && conf.server.cloudBootstrap.removeCloudFolder ) {
				FileUtils.deleteQuietly(cloudFolder);
			}
			if (jCloudsContext != null) {
				jCloudsContext.close();
			}
			serverNode.setStopped(true);
		}		
	}


	@Override
	public ServerNode bootstrapCloud(ServerNode serverNode, Conf conf) {
        
		logger.info( "Bootstrap HP cloud" );
		
		serverNode.setRemote( true );
        // get existing management machine
        List<CloudServer> existingManagementMachines = null;
        try{
        	
        	HPAdvancedParams params = getHPAdnacedParameters( serverNode );
    		String project = params.getProject();
    		String key = params.getKey();
    		String secretKey = params.getSecretKey();
        	
            existingManagementMachines = 
            		CloudifyUtils.getAllMachinesWithPredicate( new ServerNamePrefixPredicate(conf), 
            		new NovaContext( conf.server.cloudBootstrap.cloudProvider,
            				project, key, secretKey,
            				conf.server.cloudBootstrap.zoneName, true ) );
        }
        catch(Exception e){
            if ( ExceptionUtils.indexOfThrowable( e, AuthorizationException.class ) > 0 ){
                serverNode.errorEvent( "Invalid Credentials" ).save(  );
                return null;
            }
            logger.error( "unrecognized exception, assuming only existing algorithm failed. ",e );
        }

        logger.info( "found [{}] management machines", CollectionUtils.size( existingManagementMachines ) );

        if ( !CollectionUtils.isEmpty( existingManagementMachines ) ) {

        	CloudServer managementMachine = CollectionUtils.first( existingManagementMachines );

            // GUY - for some reason

            // ((Address )managementMachine.getAddresses().get("private").toArray()[1]).getAddr()

            Utils.ServerIp serverIp = Utils.getServerIp( managementMachine );

            if ( !cloudifyRestClient.testRest( serverIp.publicIp ).isSuccess() ){
                serverNode.errorEvent( "Management machine exists but unreachable" ).save(  );
                logger.info( "unable to reach management machine. stopping progress." );
                return null;
            }
            logger.info( "using first machine  [{}] with ip [{}]", managementMachine, serverIp );
            serverNode.setServerId( managementMachine.getId() );
            serverNode.infoEvent("Found management machine on :" + serverIp ).save(  );
            serverNode.setPublicIP( serverIp.publicIp );
            serverNode.save(  );
            logger.info( "not searching for key - only needed for bootstrap" );
        } 
        else {
            logger.info( "did not find an existing management machine, creating new machine" );
            createNewMachine( serverNode, conf );
        }
        
        return serverNode;
	}
	
	private HPAdvancedParams getHPAdnacedParameters( ServerNode serverNode ) {
    	JsonNode parsedParams = Json.parse( serverNode.getAdvancedParams() );
    	HPAdvancedParams params = Json.fromJson( parsedParams.get( PARAMS ), HPAdvancedParams.class );
    	return params; 
	}
	
	
    class ServerNamePrefixPredicate implements Predicate<CloudServer>{
    	
        private final String prefix;

        ServerNamePrefixPredicate( Conf conf ){
        	this.prefix = conf.server.cloudBootstrap.existingManagementMachinePrefix;
        }

        @Override
        public boolean apply( CloudServer server ){
            // return true iff server is not null, prefix is not empty and prefix is prefix of server.getName
            return server != null && !StringUtils.isEmpty( prefix ) && server.getName().indexOf( prefix ) == 0;
        }
        
        @Override
        public String toString(){
             return String.format("name has prefix [%s]", prefix);
        }
    }


	@Override
	public CloudProvider getCloudProvider() {
		// TODO Auto-generated method stub
		return CloudProvider.HP;
	}
}