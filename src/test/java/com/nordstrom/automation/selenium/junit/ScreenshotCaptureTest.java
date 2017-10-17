package com.nordstrom.automation.selenium.junit;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.model.ExamplePage;

@InitialPage(ExamplePage.class)
public class ScreenshotCaptureTest extends JUnitBase {
    
    @Test
    public void testScreenshotCapture() {
        Optional<Path> optArtifactPath = screenshotCapture.captureArtifact();
        assertTrue(optArtifactPath.isPresent());
    }

}
