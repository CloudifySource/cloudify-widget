package clouds.base;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import models.ServerNode;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.rest.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.CollectionUtils;
import utils.Utils;
import beans.api.ExecutorFactory;
import beans.cloudify.CloudifyRestClient;
import beans.config.Conf;

import com.google.common.base.Predicate;

abstract public class AbstractBootstrapCloudHandler implements BootstrapCloudHandler{

	protected static Logger logger = LoggerFactory.getLogger( AbstractBootstrapCloudHandler.class );
	
    @Inject
    protected ExecutorFactory executorFactory;
	
    @Inject
    protected CloudifyRestClient cloudifyRestClient;
	
	@Override
	public ServerNode bootstrapCloud( ServerNode serverNode, Conf conf, ComputeServiceContext computeServiceContext  ) {
		logger.info( "Bootstrap [" + getCloudProvider().label + "] cloud" );
		
		serverNode.setRemote( true );
        // get existing management machine
		Set<? extends NodeMetadata> existingManagementMachines = null;
        try{
        	AdvancedParams params = getAdvancedParameters( serverNode );
    		existingManagementMachines = listExistingManagementMachines( params, conf, computeServiceContext.getComputeService() );
        }
        catch(Exception e){
            if ( ExceptionUtils.indexOfThrowable( e, AuthorizationException.class ) > 0 ){
                serverNode.errorEvent( "Invalid Credentials" ).save(  );
                return null;
            }
            logger.error( "unrecognized exception, assuming only existing algorithm failed. ",e );
        }

        existingManagementMachines = new HashSet<NodeMetadata>();
        logger.info( "found [{}] management machines", CollectionUtils.size( existingManagementMachines ) );

        if ( !CollectionUtils.isEmpty( existingManagementMachines ) ) {

        	NodeMetadata managementMachine = CollectionUtils.first( existingManagementMachines );
            Utils.ServerIp serverIp = Utils.getServerIp( managementMachine );

            if ( !cloudifyRestClient.testRest( serverIp.publicIp ).isSuccess() ){
                serverNode.errorEvent( "Management machine exists but unreachable" ).save(  );
                logger.info( "unable to reach management machine on. stopping progress. Testet rest IP [{}]", serverIp );
                return null;
            }
            logger.info( "using first machine  [{}] with ip [{}]", managementMachine, serverIp );
            serverNode.setServerId( managementMachine.getId() );
            serverNode.infoEvent("Found management machine on :" + serverIp ).save(  );
            serverNode.setPublicIP( serverIp.publicIp );
            serverNode.save();
            logger.info( "not searching for key - only needed for bootstrap" );
        } 
        else {
            logger.info( "did not find an existing management machine, creating new machine" );
            createNewMachine( serverNode, conf, computeServiceContext );

        }
        return serverNode;
	}

	abstract protected Set<? extends NodeMetadata> listExistingManagementMachines( AdvancedParams advancedParameters, Conf conf, ComputeService computeService );
	
	abstract protected AdvancedParams getAdvancedParameters( ServerNode serverNode );
	
    public static class MachineNamePrefixPredicate implements Predicate<ComputeMetadata>{
    	
        private final String prefix;

        public MachineNamePrefixPredicate( Conf conf ){
        	prefix = conf.server.cloudBootstrap.existingManagementMachinePrefix;
        }

        @Override
        public boolean apply( ComputeMetadata computeMetadata ){
            // return true if server is not null, prefix is not empty and prefix is prefix of server.getName
        	boolean startsWithPrefix = computeMetadata.getName().startsWith( prefix );
        	logger.info( "Within MachineName apply()" +
        			", computeMetadata.getName()=" + computeMetadata.getName() + 
        			", prefix=" + prefix + 
        			", startsWithPrefix=" + startsWithPrefix );
            return !StringUtils.isEmpty( prefix ) && startsWithPrefix;
        }
        
        @Override
        public String toString(){
             return String.format("name has prefix [%s]", prefix);
        }
    }	
}