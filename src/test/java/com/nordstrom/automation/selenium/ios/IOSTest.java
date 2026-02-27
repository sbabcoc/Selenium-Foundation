package com.nordstrom.automation.selenium.ios;

import static com.nordstrom.automation.selenium.platform.TargetType.IOS_APP_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import org.testng.Reporter;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.examples.IOSApplicationEchoScreenView;
import com.nordstrom.automation.selenium.examples.IOSApplicationMainView;
import com.nordstrom.automation.selenium.listeners.PageSourceCapture;
import com.nordstrom.automation.selenium.platform.TargetPlatform;
import com.nordstrom.automation.selenium.support.TestNgTargetBase;

@InitialPage(IOSApplicationMainView.class)
public class IOSTest extends TestNgTargetBase {
    
    @Test
    @TargetPlatform(IOS_APP_NAME)
    public void testEditing() {
        IOSApplicationMainView mainView = getInitialPage();
        IOSApplicationEchoScreenView echoScreen = mainView.openEchoScreen();
        UUID uuid = UUID.randomUUID();
        echoScreen.setSavedMessage(uuid.toString());
        assertEquals(echoScreen.getSavedMessage(), uuid.toString());
    }
    
    @Test
    @TargetPlatform(IOS_APP_NAME)
    public void testIOSHierarchyCapture() {
        PageSourceCapture collector = getLinkedListener(PageSourceCapture.class);
        assertTrue(collector.getArtifactProvider().canGetArtifact(Reporter.getCurrentTestResult()));
        Optional<Path> optArtifactPath = collector.captureArtifact(Reporter.getCurrentTestResult());
        assertTrue(optArtifactPath.isPresent());
    }
    
}
