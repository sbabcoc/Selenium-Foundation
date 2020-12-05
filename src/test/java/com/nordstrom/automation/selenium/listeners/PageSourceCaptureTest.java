package com.nordstrom.automation.selenium.listeners;

import static org.testng.Assert.assertTrue;

import java.nio.file.Path;

import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.google.common.base.Optional;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.model.ExamplePage;
import com.nordstrom.automation.selenium.model.TestNgRoot;

@InitialPage(ExamplePage.class)
public class PageSourceCaptureTest extends TestNgRoot {
    
    @Test
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
