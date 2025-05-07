package com.nordstrom.automation.selenium.support;

import org.openqa.selenium.WebDriverException;
import org.testng.ITestResult;

import com.nordstrom.automation.testng.TestNGRetryAnalyzer;

/**
 * This class implements a Selenium-specific <b>TestNG</b> retry analyzer.
 */
public class RetryAnalyzer implements TestNGRetryAnalyzer {
    
    /**
     * {@inheritDoc}
     * <p>
     * This implementation deems that a test that fails with an instance of {@link WebDriverException} is re-triable.
     */
    @Override
    public boolean retry(ITestResult result) {
        return (result.getThrowable() instanceof WebDriverException);
    }

}
