package org.cloudifysource.widget.test;


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Created with IntelliJ IDEA.
 * User: sagib
 * Date: 06/01/13
 * Time: 16:33
 * To change this template use File | Settings | File Templates.
 */
public class AbstractCloudifyWidgetTest {
    protected static WebDriver webDriver;

    @BeforeSuite
    public void before(){
        webDriver = new FirefoxDriver();
    }


    @AfterSuite
    public void after(){
        webDriver.close();
    }
}