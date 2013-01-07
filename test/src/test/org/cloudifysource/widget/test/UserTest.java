package src.test.org.cloudifysource.widget.test;

import junit.framework.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Created with IntelliJ IDEA.
 * User: sagib
 * Date: 06/01/13
 * Time: 16:41
 * To change this template use File | Settings | File Templates.
 */
public class UserTest extends AbstractCloudifyWdgetTest {

    private final static String host = "localhost:9000";

    @Test
    public void loginTest(){
        webDriver.get(host + "/admin/signup");
        WebElement email = webDriver.findElement(By.name("email"));
        WebElement firstname = webDriver.findElement(By.name("firstname"));
        WebElement lastname = webDriver.findElement(By.name("lastname"));
        WebElement password = webDriver.findElement(By.name("password"));
        WebElement passwordConfirmation = webDriver.findElement(By.name("passwordConfirmation"));
        WebElement submit = webDriver.findElement(By.className("btn-primary"));
        email.sendKeys("test@test.com");
        firstname.sendKeys("test");
        lastname.sendKeys("test");
        String pass = "testTest1";
        password.sendKeys(pass);
        passwordConfirmation.sendKeys(pass);
        submit.click();
        Assert.assertTrue("Welcome is not displayed",webDriver.findElement(By.id("welcome_window")).isDisplayed());
    }
}
