package com.nordstrom.automation.selenium.listeners;

import static org.testng.Assert.assertTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.testng.Reporter;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.model.ExamplePage;
import com.nordstrom.automation.selenium.support.TestNgBase;

@InitialPage(ExamplePage.class)
public class PageSourceCaptureTest extends TestNgBase {
    
    @Test
    public void testPageSourceCapture() {
        Optional<Path> optArtifactPath = 
                        getListener(PageSourceCapture.class).captureArtifact(Reporter.getCurrentTestResult());
        assertTrue(optArtifactPath.isPresent());
    }

}
