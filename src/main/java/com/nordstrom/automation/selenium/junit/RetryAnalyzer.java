package com.nordstrom.automation.selenium.junit;

import org.junit.runners.model.FrameworkMethod;
import org.openqa.selenium.WebDriverException;

import com.nordstrom.automation.junit.JUnitRetryAnalyzer;

public class RetryAnalyzer implements JUnitRetryAnalyzer {

    @Override
    public boolean retry(FrameworkMethod method, Throwable thrown) {
        return (thrown instanceof WebDriverException);
    }

}
