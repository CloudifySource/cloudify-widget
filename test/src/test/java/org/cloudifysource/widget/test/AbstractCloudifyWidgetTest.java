package org.cloudifysource.widget.test;


import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: sagib
 * Date: 06/01/13
 * Time: 16:33
 * To change this template use File | Settings | File Templates.
 */
public class AbstractCloudifyWidgetTest {
    protected static WebDriver webDriver;
    public static final String HOST = Utils.getHost();
    public static final String PASSWORD = "testTest1";
    public static final String EMAIL = "test@test.com";

    @BeforeSuite
    public void before(){
        webDriver = new FirefoxDriver();
    }


    @AfterSuite
    public void after(){
        webDriver.close();
    }

    protected void waitForElement(final By by) {
        FluentWait<By> fw = new FluentWait<By>(by);
        fw.withTimeout(30, TimeUnit.SECONDS);
        fw.until(new Predicate<By>(){
            @Override
            public boolean apply(By input) {
                try{
                    return webDriver.findElement(by).isDisplayed();
                }catch(NoSuchElementException e){
                    return false;
                }
            }
        });
    }

    protected void login(String username, String password) {
        webDriver.get(HOST + "/admin/signin");
        webDriver.findElement(By.name("email")).sendKeys(username);
        webDriver.findElement(By.name("password")).sendKeys(password);
        webDriver.findElement(By.className("btn-primary")).click();
    }

    protected void logout() {
        webDriver.get(HOST + "/admin/widgets");
        webDriver.findElement(By.id("logout")).click();
    }

}