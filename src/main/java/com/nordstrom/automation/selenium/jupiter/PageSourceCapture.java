package com.nordstrom.automation.selenium.jupiter;

import com.nordstrom.automation.jupiter.ArtifactCollector;

/**
 * This class uses the {@link ArtifactCollector} to implement a page source capturing test watcher.
 */
public class PageSourceCapture extends ArtifactCollector<PageSourceArtifact> {

    /**
     * This constructor provides a {@link PageSourceArtifact} object to the {@link ArtifactCollector}.
     * <p>
     * <b>NOTE</b>: Unlike JUnit Foundation's {@code ArtifactCollector}, Jupiter Foundation's version
     * takes no {@code instance} parameter - it reads the test instance directly from the
     * {@code ExtensionContext} passed to {@code testFailed(...)} rather than needing it captured at
     * construction time.
     */
    public PageSourceCapture() {
        super(new PageSourceArtifact());
    }

}
