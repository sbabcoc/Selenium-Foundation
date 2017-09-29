package com.nordstrom.automation.selenium.support;

import java.util.Optional;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.nordstrom.automation.selenium.listeners.DriverManager;
import com.nordstrom.automation.selenium.listeners.ScreenshotCapture;
import com.nordstrom.automation.selenium.model.Page;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.LinkedListeners;

/**
 * This abstract class implements the contract for Selenium Foundation test classes for TestNG.
 */
@LinkedListeners({ScreenshotCapture.class, DriverManager.class, ExecutionFlowController.class})
public abstract class TestNGBase implements TestBase {
    
    /**
     * This enumeration is responsible for storing and retrieving values in the attributes collection of the current
     * test result, as reported by {@link Reporter#getCurrentTestResult()}. 
     */
    private enum TestAttributes {
        DRIVER("Driver"),
        INITIAL_PAGE("InitialPage");
        
        private String key;
        
        TestAttributes(String key) {
            this.key = key;
        }
        
        /**
         * Store the specified object in the attributes collection.
         * 
         * @param obj object to be stored; 'null' to discard value
         * @return (optional) specified object
         */
        private Optional<?> set(Object obj) {
            ITestResult result = Reporter.getCurrentTestResult();
            Optional<?> val = TestBase.optionalOf(obj);
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
        return (Optional<WebDriver>) TestAttributes.DRIVER.nab();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<WebDriver> setDriver(WebDriver driver) {
        return (Optional<WebDriver>) TestAttributes.DRIVER.set(driver);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Page> nabInitialPage() {
        return (Optional<Page>) TestAttributes.INITIAL_PAGE.nab();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Page> setInitialPage(Page initialPage) {
        return (Optional<Page>) TestAttributes.INITIAL_PAGE.set(initialPage);
    }
}
