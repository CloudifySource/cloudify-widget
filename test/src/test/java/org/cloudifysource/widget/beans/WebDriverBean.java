package org.cloudifysource.widget.beans;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.server.ClassPathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: sagib
 * Date: 23/01/13
 * Time: 11:35
 */
public class WebDriverBean {
    private static Logger logger = LoggerFactory.getLogger(WebDriverBean.class);
    private DriverType driverType = DriverType.CHROME;
    private String chromeBinPath = null; //"drivers/chrome.exe";

    public WebDriver getDriver() throws IOException, URISyntaxException {
        switch (driverType) {
            case FIREFOX: {
                FirefoxDriver wd = new FirefoxDriver();
                return wd;
            }
            case CHROME: {
                DesiredCapabilities desired = DesiredCapabilities.chrome();
                ChromeDriverService chromeService;
                ChromeDriverService.Builder builder = new ChromeDriverService.Builder().usingAnyFreePort();
                if (new File(chromeBinPath).exists()) {
                    logger.info("using file location [{}]", chromeBinPath);
                    System.setProperty("webdriver.chrome.driver", chromeBinPath);
                    builder = builder.usingDriverExecutable(new File(chromeBinPath));
                } else if (chromeBinPath != null) {
                    logger.info("finding classpath resource at :[{}]", chromeBinPath);
                    File chromeDriver = new ClassPathResource(chromeBinPath).getFile();
                    System.setProperty("webdriver.chrome.driver", chromeDriver.getAbsolutePath());
                    builder = builder.usingDriverExecutable(chromeDriver);
                }

                chromeService = builder.build();
                logger.info("Starting Chrome Driver Server...");
                chromeService.start();
                ChromeDriver wd = new ChromeDriver(chromeService, desired);
                return wd;
            }
        }
        throw new RuntimeException("no driver specified");
    }



    public void setDriverType(DriverType driverType) {
        this.driverType = driverType;
    }

    public void setChromeBinPath(String chromeBinPath) {
        this.chromeBinPath = chromeBinPath;


    }

    public static enum DriverType {
        FIREFOX, CHROME
    }
}
