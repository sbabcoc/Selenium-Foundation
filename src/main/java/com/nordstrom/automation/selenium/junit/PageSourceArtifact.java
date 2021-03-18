package com.nordstrom.automation.selenium.junit;

import java.nio.file.Path;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nordstrom.automation.selenium.core.DriverManager;
import com.nordstrom.automation.selenium.utility.PageSourceUtils;
import com.google.common.base.Optional;
import com.nordstrom.automation.junit.ArtifactType;

/**
 * This class implements the artifact type for screenshot capture.
 */
public class PageSourceArtifact extends ArtifactType {
    
    private static final String ARTIFACT_PATH = "page-source";
    private static final String EXTENSION = "html";
    private static final Logger LOGGER = LoggerFactory.getLogger(PageSourceArtifact.class);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canGetArtifact(final Object instance) {
        Optional<WebDriver> optDriver = DriverManager.nabDriver(instance);
        return PageSourceUtils.canGetArtifact(optDriver, LOGGER);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getArtifact(final Object instance, final Throwable reason) {
        Optional<WebDriver> optDriver = DriverManager.nabDriver(instance);
        return PageSourceUtils.getArtifact(optDriver, reason, LOGGER).getBytes();
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
