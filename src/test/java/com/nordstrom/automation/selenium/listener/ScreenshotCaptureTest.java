package com.nordstrom.automation.selenium.listener;

import static org.testng.Assert.assertTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.listeners.DriverManager;
import com.nordstrom.automation.selenium.listeners.ScreenshotCapture;
import com.nordstrom.automation.selenium.model.ExamplePage;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.LinkedListeners;
import com.nordstrom.automation.testng.ListenerChain;

@InitialPage(ExamplePage.class)
@LinkedListeners({ScreenshotCapture.class, DriverManager.class, ExecutionFlowController.class})
public class ScreenshotCaptureTest {
    
    @Test
    public void testScreenshotCapture() {
        ITestResult testResult = Reporter.getCurrentTestResult();
        Optional<ITestNGListener> optListener = ListenerChain.getAttachedListener(testResult, ScreenshotCapture.class);
        assertTrue(optListener.isPresent());
        ScreenshotCapture listener = (ScreenshotCapture) optListener.get();
        Optional<Path> optArtifactPath = listener.captureArtifact(testResult);
        assertTrue(optArtifactPath.isPresent());
    }

}
