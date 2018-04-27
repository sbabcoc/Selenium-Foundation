package com.nordstrom.automation.selenium.support;

import org.openqa.selenium.WebDriverException;
import org.testng.ITestResult;

import com.nordstrom.automation.testng.RetryManager;

public class RetryAnalyzer extends RetryManager {
    
    @Override
    protected boolean isRetriable(ITestResult result) {
        if (result.getThrowable() instanceof WebDriverException) {
            return true;
        }
        return super.isRetriable(result);
    }

}
