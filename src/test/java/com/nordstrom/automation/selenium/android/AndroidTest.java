package com.nordstrom.automation.selenium.android;

import static com.nordstrom.automation.selenium.platform.TargetType.ANDROID_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.testng.Reporter;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.examples.AndroidPage;
import com.nordstrom.automation.selenium.listeners.PageSourceCapture;
import com.nordstrom.automation.selenium.listeners.ScreenshotCapture;
import com.nordstrom.automation.selenium.platform.TargetPlatform;
import com.nordstrom.automation.selenium.support.TestNgTargetBase;

@InitialPage(AndroidPage.class)
public class AndroidTest extends TestNgTargetBase {
    
    @Test
    @TargetPlatform(ANDROID_NAME)
    public void testSearchActivity() {
        AndroidPage page = getInitialPage();
        page.submitSearchQuery("Hello world!");
        assertEquals(page.getSearchResult(), "Hello world!");
    }
    
    @Test
    @TargetPlatform(ANDROID_NAME)
    public void testAndroidHierarchyCapture() {
        PageSourceCapture collector = getLinkedListener(PageSourceCapture.class);
        assertTrue(collector.getArtifactProvider().canGetArtifact(Reporter.getCurrentTestResult()));
        Optional<Path> optArtifactPath = collector.captureArtifact(Reporter.getCurrentTestResult());
        assertTrue(optArtifactPath.isPresent());
    }
    
    @Test
    @TargetPlatform(ANDROID_NAME)
    public void testAndroidScreenshotCapture() {
        ScreenshotCapture collector = getLinkedListener(ScreenshotCapture.class);
        assertTrue(collector.getArtifactProvider().canGetArtifact(Reporter.getCurrentTestResult()));
        Optional<Path> optArtifactPath = collector.captureArtifact(Reporter.getCurrentTestResult());
        assertTrue(optArtifactPath.isPresent());
    }
    
}
