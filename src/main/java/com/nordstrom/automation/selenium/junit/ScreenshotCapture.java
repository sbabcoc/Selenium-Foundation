package com.nordstrom.automation.selenium.junit;

import com.nordstrom.automation.junit.ArtifactCollector;

/**
 * This class uses the {@link ArtifactCollector} to implement a screenshot capturing test watcher.
 */
public class ScreenshotCapture extends ArtifactCollector<ScreenshotArtifact> {
    
    /**
     * This constructor provides a {@link ScreenshotArtifact} object to the {@link ArtifactCollector}.
     * 
     * @param instance JUnit test class instance
     */
    public ScreenshotCapture(final Object instance) {
        super(instance, new ScreenshotArtifact());
    }

}
