package com.nordstrom.automation.selenium.listeners;

import com.nordstrom.automation.testng.ArtifactCollector;

public class ScreenshotCapture extends ArtifactCollector<ScreenshotArtifact> {
    
    public ScreenshotCapture() {
        super(new ScreenshotArtifact());
    }

}
