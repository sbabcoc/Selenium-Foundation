package com.nordstrom.automation.selenium.junit;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.model.ExamplePage;
import com.nordstrom.common.file.PathUtils;

@InitialPage(ExamplePage.class)
public class PageSourceCaptureTest extends JUnitBase {
    
    @Test
    public void testPageSourceCapture() {
        Optional<Path> optArtifactPath = getLinkedRule(PageSourceCapture.class).captureArtifact(null);
        assertTrue(optArtifactPath.isPresent());
    }

    @Override
    public String getOutputDirectory() {
        return PathUtils.ReportsDirectory.ARTIFACT.getPath().toString();
    }

}
