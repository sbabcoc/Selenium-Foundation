package com.nordstrom.automation.selenium.junit;

import org.junit.runners.model.FrameworkMethod;
import org.openqa.selenium.WebDriverException;

import com.nordstrom.automation.junit.JUnitRetryAnalyzer;

/**
 * This class implements a Selenium-specific <b>JUnit</b> retry analyzer.
 */
public class RetryAnalyzer implements JUnitRetryAnalyzer {

    /**
     * {@inheritDoc}
     * <p>
     * This implementation deems that a test that fails with an instance of {@link WebDriverException} is re-triable.
     */
    @Override
    public boolean retry(FrameworkMethod method, Throwable thrown) {
        return (thrown instanceof WebDriverException);
    }

}
