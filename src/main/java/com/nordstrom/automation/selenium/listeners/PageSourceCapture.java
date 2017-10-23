package com.nordstrom.automation.selenium.listeners;

import com.nordstrom.automation.testng.ArtifactCollector;

/**
 * This class uses the {@link ArtifactCollector} to implement a page source capturing listener.
 */
public class PageSourceCapture extends ArtifactCollector<PageSourceArtifact> {
    
    /**
     * This constructor provides a {@link PageSourceArtifact} object to the {@link ArtifactCollector}.
     */
    public PageSourceCapture() {
        super(new PageSourceArtifact());
    }

}
