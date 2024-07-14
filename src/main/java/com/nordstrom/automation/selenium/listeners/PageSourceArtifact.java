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
    private static final String EXTENSION = "html";
    private static final Logger LOGGER = LoggerFactory.getLogger(PageSourceArtifact.class);
    
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
    public byte[] getArtifact(final ITestResult result) {
        // ensure current test result is set
        Reporter.setCurrentTestResult(result);
        Optional<WebDriver> optDriver = DriverManager.nabDriver(result.getInstance());
        return PageSourceUtils.getArtifact(optDriver, result.getThrowable(), LOGGER).getBytes();
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
