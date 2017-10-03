package com.nordstrom.automation.selenium.support;

import java.lang.reflect.Method;
import java.util.Optional;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.core.TestBase;
import com.nordstrom.automation.selenium.listeners.DriverListener;
import com.nordstrom.automation.selenium.listeners.ScreenshotCapture;
import com.nordstrom.automation.selenium.model.Page;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.LinkedListeners;

/**
 * This abstract class implements the contract for Selenium Foundation test classes for TestNG.
 */
@LinkedListeners({ScreenshotCapture.class, DriverListener.class, ExecutionFlowController.class})
public abstract class TestNgBase implements TestBase {
    
    /**
     * This enumeration is responsible for storing and retrieving values in the attributes collection of the current
     * test result, as reported by {@link Reporter#getCurrentTestResult()}. 
     */
    private enum TestAttribute {
        DRIVER("Driver"),
        INITIAL_PAGE("InitialPage");
        
        private String key;
        
        TestAttribute(String key) {
            this.key = key;
        }
        
        /**
         * Store the specified object in the attributes collection.
         * 
         * @param obj object to be stored; 'null' to discard value
         * @return (optional) specified object
         */
        private <T> Optional<T> set(T obj) {
            ITestResult result = Reporter.getCurrentTestResult();
            Optional<T> val = TestBase.optionalOf(obj);
            result.setAttribute(key, val);
            return val;
        }
        
        /**
         * If present, get the object from the attributes collection.
         * 
         * @return (optional) stored object
         */
        private Optional<?> nab() {
            ITestResult result = Reporter.getCurrentTestResult();
            Object val = result.getAttribute(key);
            if (val != null) {
                return (Optional<?>) val;
            } else {
                return set(null);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<WebDriver> nabDriver() {
        return (Optional<WebDriver>) TestAttribute.DRIVER.nab();
    }

    @Override
    public Optional<WebDriver> setDriver(WebDriver driver) {
        return TestAttribute.DRIVER.set(driver);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Page> nabInitialPage() {
        return (Optional<Page>) TestAttribute.INITIAL_PAGE.nab();
    }

    @Override
    public Optional<Page> setInitialPage(Page initialPage) {
        return TestAttribute.INITIAL_PAGE.set(initialPage);
    }
    
    @Override
    public String getOutputDirectory() {
        ITestResult testResult = Reporter.getCurrentTestResult();
        if (testResult != null) {
            return testResult.getTestContext().getOutputDirectory();
        } else {
            return TestBase.getOutputDir();
        }
    }
    
    @Override
    public void adjustTimeout(TestBase instance, long adjust) {
        ITestResult testResult = Reporter.getCurrentTestResult();
        if (testResult != null) {
            long timeout = testResult.getMethod().getTimeOut();
            if (timeout > 0) {
                testResult.getMethod().setTimeOut(timeout + adjust);
            }
        }
    }

    @Override
    public boolean isTest(Method method) {
        return null != method.getAnnotation(Test.class);
    }
    
    @Override
    public boolean isBeforeMethod(Method method) {
        return null != method.getAnnotation(BeforeMethod.class);
    }
    
    @Override
    public boolean isAfterMethod(Method method) {
        return null != method.getAnnotation(AfterMethod.class);
    }
    
    @Override
    public boolean isBeforeClass(Method method) {
        return null != method.getAnnotation(BeforeClass.class);
    }
    
    @Override
    public boolean isAfterClass(Method method) {
        return null != method.getAnnotation(AfterClass.class);
    }
}
