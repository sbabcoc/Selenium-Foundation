package com.nordstrom.automation.selenium.junit;

import java.nio.file.Path;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nordstrom.automation.selenium.core.DriverManager;
import com.nordstrom.automation.selenium.utility.ScreenshotUtils;
import com.google.common.base.Optional;
import com.nordstrom.automation.junit.ArtifactType;

/**
 * This class implements the artifact type for screenshot capture.
 */
public class ScreenshotArtifact extends ArtifactType {
    
    private static final String ARTIFACT_PATH = "screenshots";
    private static final String EXTENSION = "png";
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenshotArtifact.class);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canGetArtifact(final Object instance) {
        Optional<WebDriver> optDriver = DriverManager.nabDriver(instance);
        return ScreenshotUtils.canGetArtifact(optDriver, LOGGER);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getArtifact(final Object instance, final Throwable reason) {
        Optional<WebDriver> optDriver = DriverManager.nabDriver(instance);
        return ScreenshotUtils.getArtifact(optDriver, reason, LOGGER);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Path getArtifactPath(final Object instance) {
        return super.getArtifactPath(instance).resolve(ARTIFACT_PATH);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getArtifactExtension() {
        return EXTENSION;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Logger getLogger() {
        return LOGGER;
    }

}
