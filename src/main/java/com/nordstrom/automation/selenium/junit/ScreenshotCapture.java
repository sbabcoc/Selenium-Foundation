package com.nordstrom.automation.selenium.junit;

import com.nordstrom.automation.junit.ArtifactCollector;

/**
 * This class uses the {@link ArtifactCollector} to implement a screenshot capturing listener.
 */
public class ScreenshotCapture extends ArtifactCollector<ScreenshotArtifact> {
    
    /**
     * This constructor provides a {@link ScreenshotArtifact} object to the {@link ArtifactCollector}.
     */
    public ScreenshotCapture(Object instance) {
        super(instance, new ScreenshotArtifact());
    }

}
