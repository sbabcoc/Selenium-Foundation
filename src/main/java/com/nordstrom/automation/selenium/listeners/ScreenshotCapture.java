package com.nordstrom.automation.selenium.listeners;

import com.nordstrom.automation.testng.ArtifactCollector;

/**
 * This class uses the {@link ArtifactCollector} to implement a screenshot capturing listener.
 */
public class ScreenshotCapture extends ArtifactCollector<ScreenshotArtifact> {
    
    /**
     * This constructor provides a {@link ScreenshotArtifact} object to the {@link ArtifactCollector}.
     */
    public ScreenshotCapture() {
        super(new ScreenshotArtifact());
    }

}
