package com.nordstrom.automation.selenium.junit;

import java.nio.file.Path;
import java.util.Optional;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nordstrom.automation.selenium.core.DriverManager;
import com.nordstrom.automation.selenium.utility.PageSourceUtils;
import com.nordstrom.automation.junit.ArtifactType;

/**
 * This class implements the artifact type for screenshot capture.
 */
public class PageSourceArtifact extends ArtifactType {
    
    private static final String ARTIFACT_PATH = "page-source";
    private static final Logger LOGGER = LoggerFactory.getLogger(PageSourceArtifact.class);
    
    private String memoizedExtension;
    
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
        memoizedExtension = null;
        
        Optional<byte[]> optArtifact = DriverManager.nabDriver(instance)
                .map(driver -> PageSourceUtils.getArtifact(Optional.of(driver), reason, LOGGER));

        return optArtifact
                .filter(bytes -> bytes.length > 0)
                .map(bytes -> {
                    memoizedExtension = PageSourceUtils.isXml(bytes) ? "xml" : "html";
                    return bytes;
                })                
                .orElse(new byte[0]);
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
        return Optional.ofNullable(memoizedExtension).orElse("html");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Logger getLogger() {
        return LOGGER;
    }

}
