package com.nordstrom.automation.selenium.junit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.nio.file.Path;

import org.junit.Test;

import com.google.common.base.Optional;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.model.ExamplePage;
import com.nordstrom.common.file.PathUtils;

@InitialPage(ExamplePage.class)
public class PageSourceCaptureTest extends JUnitRoot {
    
    @Test
    public void testPageSourceCapture() {
        PageSourceCapture collector = pageSourceCapture;
        assumeTrue(collector.getArtifactProvider().canGetArtifact(this));
        Optional<Path> optArtifactPath = collector.captureArtifact(null);
        assertTrue(optArtifactPath.isPresent());
    }

    @Override
    public String getOutputDirectory() {
        return PathUtils.ReportsDirectory.ARTIFACT.getPath().toString();
    }

}
