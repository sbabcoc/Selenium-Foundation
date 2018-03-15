package com.nordstrom.automation.selenium.junit;

import com.nordstrom.automation.junit.ArtifactCollector;

/**
 * This class uses the {@link ArtifactCollector} to implement a page source capturing test watcher.
 */
public class PageSourceCapture extends ArtifactCollector<PageSourceArtifact> {
    
    /**
     * This constructor provides a {@link PageSourceArtifact} object to the {@link ArtifactCollector}.
     * 
     * @param instance JUnit test class instance
     */
    public PageSourceCapture(final Object instance) {
        super(instance, new PageSourceArtifact());
    }

}
