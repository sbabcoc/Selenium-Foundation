package com.nordstrom.automation.selenium.windows;

import static com.nordstrom.automation.selenium.platform.TargetType.WINDOWS_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.openqa.selenium.Keys;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.examples.NotepadApplication;
import com.nordstrom.automation.selenium.listeners.PageSourceCapture;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.platform.TargetPlatform;
import com.nordstrom.automation.selenium.support.TestNgTargetBase;

@InitialPage(NotepadApplication.class)
public class WindowsTest extends TestNgTargetBase {
    
    @Test
    @TargetPlatform(WINDOWS_NAME)
    public void testEditing() {
        NotepadApplication page = getInitialPage();
        page.modifyDocument("Hello world!");
        assertEquals(page.getDocumentContent(), "Hello world!");
        page.modifyDocument(Keys.CONTROL + "z");
    }
    
    @Test
    @TargetPlatform(WINDOWS_NAME)
    public void testWindowsHierarchyCapture() {
        PageSourceCapture collector = getLinkedListener(PageSourceCapture.class);
        assertTrue(collector.getArtifactProvider().canGetArtifact(Reporter.getCurrentTestResult()));
        Optional<Path> optArtifactPath = collector.captureArtifact(Reporter.getCurrentTestResult());
        assertTrue(optArtifactPath.isPresent());
    }
    
}
