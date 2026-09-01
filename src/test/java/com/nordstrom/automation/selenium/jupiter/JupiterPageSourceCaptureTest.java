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
 * Jupiter-native equivalent of {@code PageSourceCaptureTest} - real, on-demand capture against a live
 * browser session, not a mocked one (see {@code PageSourceArtifactTest} for the mocked-driver unit
 * tests covering the artifact logic itself in isolation).
 */
@InitialPage(ExamplePage.class)
public class JupiterPageSourceCaptureTest extends JupiterTargetRoot {

    @Test
    public void testPageSourceCapture() {
        assumeTrue(pageSourceCapture.getArtifactProvider().canGetArtifact(this));
        Optional<Path> optArtifactPath = capturePageSource(null);
        assertTrue(optArtifactPath.isPresent());
    }

    @Override
    public String getOutputDirectory() {
        return PathUtils.ReportsDirectory.ARTIFACT.getPath().toString();
    }
}
