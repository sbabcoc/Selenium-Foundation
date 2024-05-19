package com.nordstrom.automation.selenium.listeners;

import static org.testng.Assert.assertTrue;
import static com.nordstrom.automation.selenium.platform.TargetType.WEB_APP_NAME;

import java.nio.file.Path;
import java.util.Optional;

import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.examples.ExamplePage;
import com.nordstrom.automation.selenium.examples.TestNgTargetRoot;
import com.nordstrom.automation.selenium.platform.TargetPlatform;

@InitialPage(ExamplePage.class)
public class ScreenshotCaptureTest extends TestNgTargetRoot {
    
    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testScreenshotCapture() {
        ScreenshotCapture collector = getLinkedListener(ScreenshotCapture.class);
        if (collector.getArtifactProvider().canGetArtifact(Reporter.getCurrentTestResult())) {
            Optional<Path> optArtifactPath = collector.captureArtifact(Reporter.getCurrentTestResult());
            assertTrue(optArtifactPath.isPresent());
        } else {
            throw new SkipException("This driver is not able to take screenshots."); 
        }
    }

}
