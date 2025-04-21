package com.nordstrom.automation.selenium.support;

import org.openqa.selenium.WebDriverException;
import org.testng.ITestResult;

import com.nordstrom.automation.testng.RetryManager;

/**
 * This class implements a Selenium-specific <b>TestNG</b> retry analyzer.
 */
public class RetryAnalyzer extends RetryManager {
    
    /**
     * {@inheritDoc}
     * <p>
     * This implementation deems that a test that fails with an instance of {@link WebDriverException} is re-triable.
     */
    @Override
    protected boolean isRetriable(ITestResult result) {
        if (result.getThrowable() instanceof WebDriverException) {
            return true;
        }
        return super.isRetriable(result);
    }

}
