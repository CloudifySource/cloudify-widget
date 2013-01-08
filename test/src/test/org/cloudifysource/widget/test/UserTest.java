package src.test.org.cloudifysource.widget.test;


import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created with IntelliJ IDEA.
 * User: sagib
 * Date: 06/01/13
 * Time: 16:41
 * To change this template use File | Settings | File Templates.
 */

public class UserTest extends AbstractCloudifyWdgetTest {

    private final static String host = "localhost:9000";

    @Test(groups = "subscribe")
    public void subscribeTest(){
        webDriver.get(host + "/admin/signup");
        WebElement email = webDriver.findElement(By.name("email"));
        WebElement firstName = webDriver.findElement(By.name("firstname"));
        WebElement lastName = webDriver.findElement(By.name("lastname"));
        WebElement password = webDriver.findElement(By.name("password"));
        WebElement passwordConfirmation = webDriver.findElement(By.name("passwordConfirmation"));
        WebElement submit = webDriver.findElement(By.className("btn-primary"));
        email.sendKeys("test@test.com");
        firstName.sendKeys("test");
        lastName.sendKeys("test");
        String pass = "testTest1";
        password.sendKeys(pass);
        passwordConfirmation.sendKeys(pass);
        submit.click();
        Assert.assertTrue(webDriver.findElement(By.id("welcome_window")).isDisplayed());
    }

    @Test(dependsOnMethods = "subscribeTest")
    public void loginTest(){

    }

    @Test(groups = "endSession", dependsOnMethods = "loginTest")
    public void logoutTest(){

    }

    @Test(groups = "endSession", dependsOnMethods = "logoutTest")
    public void unSubscribeTest(){

    }
}
