package com.nordstrom.automation.selenium.junit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.Test;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.examples.ExamplePage;
import com.nordstrom.automation.selenium.examples.JUnitTargetRoot;
import com.nordstrom.common.file.PathUtils;

@InitialPage(ExamplePage.class)
public class ScreenshotCaptureTest extends JUnitTargetRoot {
    
    @Test
    public void testScreenshotCapture() {
        ScreenshotCapture collector = screenshotCapture;
        assumeTrue(collector.getArtifactProvider().canGetArtifact(this));
        Optional<Path> optArtifactPath = collector.captureArtifact(null);
        assertTrue(optArtifactPath.isPresent());
    }

    @Override
    public String getOutputDirectory() {
        return PathUtils.ReportsDirectory.ARTIFACT.getPath().toString();
    }

}
