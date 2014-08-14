package mocks.cloud;

import cloudify.widget.api.clouds.MachineOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/13/14
 * Time: 11:38 AM
 */
public class MachineOptionsMock implements MachineOptions {
    private static Logger logger = LoggerFactory.getLogger(MachineOptionsMock.class);

    @Override
    public String getMask() {
        logger.info("getting mock mask");
        return "mock-mask";
    }
}
