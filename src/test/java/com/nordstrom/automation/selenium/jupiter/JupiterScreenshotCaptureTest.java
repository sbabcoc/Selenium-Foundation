package com.nordstrom.automation.selenium.jupiter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.examples.ExamplePage;
import com.nordstrom.automation.selenium.examples.JupiterTargetRoot;
import com.nordstrom.common.file.PathUtils;

/**
 * Jupiter-native equivalent of {@code ScreenshotCaptureTest}.
 */
@InitialPage(ExamplePage.class)
public class JupiterScreenshotCaptureTest extends JupiterTargetRoot {

    @Test
    public void testScreenshotCapture() {
        assumeTrue(screenshotCapture.getArtifactProvider().canGetArtifact(this));
        Optional<Path> optArtifactPath = captureScreenshot(null);
        assertTrue(optArtifactPath.isPresent());
    }

    @Override
    public String getOutputDirectory() {
        return PathUtils.ReportsDirectory.ARTIFACT.getPath().toString();
    }
}
