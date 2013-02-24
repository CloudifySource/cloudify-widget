package org.cloudifysource.widget.beans;

import com.google.common.base.Predicate;
import org.junit.Assert;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: sagib
 * Date: 24/01/13
 * Time: 12:13
 */
public class JCloudsContext {
    public static TestConf testConf(){
        return TestContext.get().getTestConf();
    }

    public static final String PROVIDER_OR_API = testConf().getProviderOrAPI();
    public static final String IDENTITY = testConf().getIdentity();
    public static final String CREDENTIAL = testConf().getCredential();
    public static final String MANAGEMENT_MACHINE_ID = testConf().getManagementMachineId();
    public static final String LOCATION = testConf().getLocation();
    private final ComputeService service;
    private final ComputeServiceContext context;

    private static Logger logger = LoggerFactory.getLogger(JCloudsContext.class);

    public JCloudsContext(){
        System.out.println(IDENTITY + CREDENTIAL + PROVIDER_OR_API + LOCATION + MANAGEMENT_MACHINE_ID);
        ContextBuilder builder = ContextBuilder.newBuilder(PROVIDER_OR_API);
        builder.credentials(IDENTITY, CREDENTIAL);
        context = builder.build(ComputeServiceContext.class);
        service = context.getComputeService();
    }

    public void waitForMinMachines(int minMachins, long timout){
        boolean enough = false;
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timout && !enough){
            try{
                int machines = getNumberOfMachines();
                logger.info("got {} machines", machines);
                Assert.assertTrue(minMachins <= machines);
                enough = true;
            }catch (AssertionError e){}
        }
        if(!enough){
            throw new AssertionError("didn't get " + minMachins + " machine in " + timout);
        }
    }

    private int getNumberOfMachines() {
        return service.listNodesDetailsMatching(new Predicate<ComputeMetadata>() {
            @Override
            public boolean apply(@Nullable ComputeMetadata input) {
                return input.getId().startsWith(LOCATION)
                        && !input.getId().contains(MANAGEMENT_MACHINE_ID);
            }
        }).size();
    }


    public void killNodes(){
        logger.info("killing all nodes except for management");
        service.destroyNodesMatching(new Predicate<NodeMetadata>() {
            @Override
            public boolean apply(@Nullable NodeMetadata input) {
                return input.getId().startsWith(LOCATION)
                        && !input.getId().contains(MANAGEMENT_MACHINE_ID);
            }
        });
    }

    public void close(){
        context.close();
    }

    public static void main(String ... args){
        JCloudsContext jCloudsContext = new JCloudsContext();
        jCloudsContext.killNodes();
        jCloudsContext.close();
    }
}
