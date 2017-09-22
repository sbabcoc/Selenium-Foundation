package com.nordstrom.automation.selenium.listeners;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

import com.nordstrom.automation.testng.ArtifactType;

public class ScreenshotArtifact implements ArtifactType {
    
    private static final Path ARTIFACT_PATH = Paths.get("screenshots");
    private static final String EXTENSION = "png";
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenshotArtifact.class);

    @Override
    public boolean canGetArtifact(ITestResult result) {
        WebDriver driver = DriverManager.getDriver(result);
        Boolean canTakeScreenshot = driver instanceof TakesScreenshot;
        if (!canTakeScreenshot) {
            String message =
                    "This driver is not capable of taking a screenshot.  If a screenshot is desired, use a WebDriver "
                    + "implementation that supports screenshots. https://seleniumhq.github.io/selenium/docs/api/java/"
                    + "org/openqa/selenium/TakesScreenshot.html";
            LOGGER.warn(message);
        }
        return canTakeScreenshot;
    }

    @Override
    public byte[] getArtifact(ITestResult result) {
        try {
            WebDriver driver = DriverManager.getDriver(result);
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (WebDriverException e) {
            LOGGER.warn("The driver is capable of taking a screenshot, but it failed.", e);
            return new byte[0];
        }
    }

    @Override
    public Path getArtifactPath(ITestResult result) {
        return ARTIFACT_PATH;
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
