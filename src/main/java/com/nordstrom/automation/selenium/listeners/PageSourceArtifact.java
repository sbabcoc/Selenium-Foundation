package com.nordstrom.automation.selenium.listeners;

import java.nio.file.Path;
import java.util.Optional;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.nordstrom.automation.selenium.core.DriverManager;
import com.nordstrom.automation.selenium.utility.PageSourceUtils;
import com.nordstrom.automation.testng.ArtifactType;

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
    public boolean canGetArtifact(final ITestResult result) {
        // ensure current test result is set
        Reporter.setCurrentTestResult(result);
        Optional<WebDriver> optDriver = DriverManager.nabDriver(result.getInstance());
        return PageSourceUtils.canGetArtifact(optDriver, LOGGER);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getArtifact(ITestResult result) {
        memoizedExtension = null;
        
        // ensure current test result is set
        Reporter.setCurrentTestResult(result);
        Optional<byte[]> optArtifact = DriverManager.nabDriver(result.getInstance())
                .map(driver -> PageSourceUtils.getArtifact(Optional.of(driver), result.getThrowable(), LOGGER));

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
    public Path getArtifactPath(final ITestResult result) {
        return super.getArtifactPath(result).resolve(ARTIFACT_PATH);
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
