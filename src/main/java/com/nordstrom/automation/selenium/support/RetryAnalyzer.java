package com.nordstrom.automation.selenium.support;

import org.openqa.selenium.WebDriverException;
import org.testng.ITestResult;

import com.nordstrom.automation.testng.TestNGRetryAnalyzer;

public class RetryAnalyzer implements TestNGRetryAnalyzer {
    
    @Override
    public boolean retry(ITestResult result) {
        return (result.getThrowable() instanceof WebDriverException);
    }

}
