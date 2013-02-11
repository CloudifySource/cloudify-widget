package org.cloudifysource.widget.beans;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.inject.Inject;

/**
 * User: sagib
 * Date: 15/01/13
 * Time: 11:41
 */
public class TestContext {
    private static Logger logger = LoggerFactory.getLogger(TestContext.class);
    @Inject
    private TestConf testConf;

    @Inject
    private WebDriver webDriver;

    private static TestContext instance;

    static {
        String context = null;
        try {
            context = System.getProperty("test.context");
            if (StringUtils.isEmpty(context)) {
                context = "base-suite-context.xml";
            }
            ApplicationContext myContext = new ClassPathXmlApplicationContext(context);
            instance = myContext.getBean("testContext", TestContext.class);
        } catch (Exception e) {
            logger.error(String.format("unable to load spring context %s", context), e);
            throw new RuntimeException("unable to load context", e);
        }
    }



    public  WebDriver getWebDriver(){
        return webDriver;
    }

    public static TestContext get(){
        return instance;
    }

    public TestConf getTestConf() {
        return testConf;
    }

    public void setTestConf(TestConf testConf) {
        this.testConf = testConf;
    }


}


