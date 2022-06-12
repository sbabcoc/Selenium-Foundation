package com.nordstrom.automation.selenium.listeners;

import static org.testng.Assert.assertTrue;
import static com.nordstrom.automation.selenium.platform.TargetType.WEB_APP_NAME;

import java.nio.file.Path;

import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.google.common.base.Optional;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.examples.ExamplePage;
import com.nordstrom.automation.selenium.examples.TestNgTargetRoot;
import com.nordstrom.automation.selenium.platform.TargetPlatform;

@InitialPage(ExamplePage.class)
public class PageSourceCaptureTest extends TestNgTargetRoot {
    
    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testPageSourceCapture() {
        PageSourceCapture collector = getLinkedListener(PageSourceCapture.class);
        if (collector.getArtifactProvider().canGetArtifact(Reporter.getCurrentTestResult())) {
            Optional<Path> optArtifactPath = collector.captureArtifact(Reporter.getCurrentTestResult());
            assertTrue(optArtifactPath.isPresent());
        } else {
            throw new SkipException("This driver is not able to capture page source."); 
        }
    }

}
