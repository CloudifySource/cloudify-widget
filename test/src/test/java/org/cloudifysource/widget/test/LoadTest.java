package org.cloudifysource.widget.test;

import org.junit.Test;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created with IntelliJ IDEA.
 * User: sagib
 * Date: 13/01/13
 * Time: 15:41
 * To change this template use File | Settings | File Templates.
 */
public class LoadTest extends AbstractCloudifyWidgetTest{
    private final int N = 30;
    private static Logger logger = LoggerFactory.getLogger(LoadTest.class);

    @Test
    public void loadTest(){
        logger.info("running test on [{}]", context().getTestConf().getHost());
        String apiKey = createWidget();
        logger.info("get Api key: " + apiKey);
        for (int i = 0; i < N; i++){
            webDriver.get(Utils.getHost() + "/widget/previewWidget?apiKey=" + apiKey);
            By startBtn = By.id("start_btn");
           // waitForElement(startBtn);
            //webDriver.findElement(startBtn).click();
            dropSession();
            //Assert.assertThat("widget deployed");
        }


    }



    private String createWidget() {
        By createButton = By.className("btn-primary");
        waitForElement(createButton);
        webDriver.findElement(createButton).click();
        By productName = By.name("productName");
        By rootpath = By.id("rootpath");
        waitForElement(productName);
        waitForElement(rootpath);
        webDriver.findElement(productName).sendKeys(NAME);
        webDriver.findElement(By.id("productVersion")).sendKeys(NAME);
        webDriver.findElement(By.id("title")).sendKeys(NAME);
        webDriver.findElement(By.id("providerURL")).sendKeys(NAME + ".com");
        webDriver.findElement(rootpath).sendKeys("tomcat/");
        webDriver.findElement(By.id("recipeURL")).sendKeys("http://dl.dropbox.com/u/8720454/tomcat.zip");
        webDriver.findElement(By.xpath("/html/body/div[@id='new_widget_modal']/form[@id='new_widget_form']/div[@class='modal-footer']/input[@class='btn btn-primary']")).click();
        By widget = By.className("enabled-widget");
        waitForElement(widget);
        return webDriver.findElement(widget).getAttribute("data-api_key");

    }
}
