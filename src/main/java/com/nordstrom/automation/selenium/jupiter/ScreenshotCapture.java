package com.nordstrom.automation.selenium.jupiter;

import com.nordstrom.automation.jupiter.ArtifactCollector;

/**
 * This class uses the {@link ArtifactCollector} to implement a screenshot capturing test watcher.
 */
public class ScreenshotCapture extends ArtifactCollector<ScreenshotArtifact> {

    /**
     * This constructor provides a {@link ScreenshotArtifact} object to the {@link ArtifactCollector}.
     * <p>
     * <b>NOTE</b>: See {@link PageSourceCapture}'s constructor javadoc for why this takes no
     * {@code instance} parameter, unlike its JUnit Foundation counterpart.
     */
    public ScreenshotCapture() {
        super(new ScreenshotArtifact());
    }

}
