package com.nordstrom.automation.selenium.junit;

import java.util.Optional;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.core.DriverManager;
import com.nordstrom.automation.selenium.core.TestBase;
import com.nordstrom.automation.junit.ArtifactType;

/**
 * This class implements the artifact type for screenshot capture.
 */
public class ScreenshotArtifact implements ArtifactType {
    
    private static final String EXTENSION = "png";
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenshotArtifact.class);

    @Override
    public boolean canGetArtifact(Object instance) {
        Optional<WebDriver> optDriver = DriverManager.nabDriver(instance);
        if (optDriver.isPresent()) {
            if (optDriver.get() instanceof TakesScreenshot) {
                return true;
            } else {
                String message =
                        "This driver is not capable of taking a screenshot.  If a screenshot is desired, use a WebDriver "
                        + "implementation that supports screenshots. https://seleniumhq.github.io/selenium/docs/api/java/"
                        + "org/openqa/selenium/TakesScreenshot.html";
                LOGGER.warn(message);
            }
        }
        return false;
    }

    @Override
    public byte[] getArtifact(Object instance) {
        if (canGetArtifact(instance)) {
            try {
                TestBase testBase = (TestBase) instance;
                WebDriver driver = testBase.getDriver();
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            } catch (WebDriverException e) {
                LOGGER.warn("The driver is capable of taking a screenshot, but it failed.", e);
            }
        }
        return new byte[0];
    }

    @Override
    public String getArtifactExtension() {
        return EXTENSION;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

}
