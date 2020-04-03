package com.nordstrom.automation.selenium.junit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.nio.file.Path;

import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.google.common.base.Optional;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.exceptions.PlatformActivationFailedException;
import com.nordstrom.automation.selenium.model.ExamplePage;
import com.nordstrom.automation.selenium.platform.ExamplePlatform;
import com.nordstrom.automation.selenium.platform.PlatformEnum;
import com.nordstrom.automation.selenium.platform.PlatformTargetable;
import com.nordstrom.automation.selenium.platform.TargetPlatform;
import com.nordstrom.common.file.PathUtils;

@InitialPage(ExamplePage.class)
public class ScreenshotCaptureTest extends JUnitBase implements PlatformTargetable<ExamplePlatform> {
    
    @Test
    public void testScreenshotCapture() {
        ScreenshotCapture collector = getLinkedRule(ScreenshotCapture.class);
        assumeTrue(collector.getArtifactProvider().canGetArtifact(this));
        Optional<Path> optArtifactPath = collector.captureArtifact(null);
        assertTrue(optArtifactPath.isPresent());
    }

    @Test
    @TargetPlatform(ExamplePlatform.PLATFORM_TWO_)
    public void testScreenshotCaptureTwo() {
        ScreenshotCapture collector = getLinkedRule(ScreenshotCapture.class);
        assumeTrue(collector.getArtifactProvider().canGetArtifact(this));
        Optional<Path> optArtifactPath = collector.captureArtifact(null);
        assertTrue(optArtifactPath.isPresent());
    }

    @Override
    public String getOutputDirectory() {
        return PathUtils.ReportsDirectory.ARTIFACT.getPath().toString();
    }

    @Override
    public ExamplePlatform getTargetPlatform() {
        return (ExamplePlatform) targetPlatformRule.getPlatform();
    }

    @Override
    public void activatePlatform(WebDriver driver, PlatformEnum platformEnum) throws PlatformActivationFailedException {
        // by default, do nothing
    }

    @Override
    public ExamplePlatform[] getValidPlatforms() {
        return ExamplePlatform.values();
    }

    @Override
    public ExamplePlatform getDefaultPlatform() {
        return ExamplePlatform.PLATFORM_ONE;
    }

    @Override
    public ExamplePlatform platformFromString(String name) {
        return ExamplePlatform.fromString(name);
    }

    @Override
    public Class<ExamplePlatform> getPlatformType() {
        return ExamplePlatform.class;
    }

    @Override
    public String[] getSubPath() {
        return PathUtils.append(getTargetPlatform().getName());
    }

}
