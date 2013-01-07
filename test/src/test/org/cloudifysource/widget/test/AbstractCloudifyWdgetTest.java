package src.test.org.cloudifysource.widget.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * Created with IntelliJ IDEA.
 * User: sagib
 * Date: 06/01/13
 * Time: 16:33
 * To change this template use File | Settings | File Templates.
 */
public class AbstractCloudifyWdgetTest {
    protected static WebDriver webDriver;

    @BeforeClass
    public static void before(){
        webDriver = new FirefoxDriver();
    }


    @AfterClass
    public static void after(){
        webDriver.close();
    }
}