package com.nordstrom.automation.selenium.support;

import java.lang.reflect.Method;
import java.util.Optional;

import org.openqa.selenium.WebDriver;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.core.TestBase;
import com.nordstrom.automation.selenium.listeners.DriverListener;
import com.nordstrom.automation.selenium.listeners.PageSourceCapture;
import com.nordstrom.automation.selenium.listeners.ScreenshotCapture;
import com.nordstrom.automation.selenium.model.Page;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.LinkedListeners;
import com.nordstrom.automation.testng.ListenerChain;
import com.nordstrom.automation.testng.TestNGConfig.TestNGSettings;
import com.nordstrom.common.file.PathUtils;

/**
 * This abstract class implements the contract for Selenium Foundation test classes for TestNG.
 */
@LinkedListeners(
        {ScreenshotCapture.class, PageSourceCapture.class, DriverListener.class, ExecutionFlowController.class})
public abstract class TestNgBase implements TestBase {
    
    static {
        System.setProperty(TestNGSettings.RETRY_ANALYZER.key(), RetryAnalyzer.class.getName());
    }
    
    /**
     * This enumeration is responsible for storing and retrieving values in the attributes collection of the current
     * test result, as reported by {@link Reporter#getCurrentTestResult()}. 
     */
    private enum TestAttribute {
        DRIVER("Driver"),
        INITIAL_PAGE("InitialPage");
        
        private String key;
        
        /**
         * Constructor for TestAttribute enumeration
         * 
         * @param key key for this constant
         */
        TestAttribute(final String key) {
            this.key = key;
        }
        
        /**
         * Store the specified object in the attributes collection.
         * 
         * @param obj object to be stored; 'null' to discard value
         * @return (optional) specified object
         */
        private <T> Optional<T> set(final T obj) {
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Optional<WebDriver> nabDriver() {
        return (Optional<WebDriver>) TestAttribute.DRIVER.nab();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<WebDriver> setDriver(final WebDriver driver) {
        return TestAttribute.DRIVER.set(driver);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Optional<Page> nabInitialPage() {
        return (Optional<Page>) TestAttribute.INITIAL_PAGE.nab();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Page> setInitialPage(final Page initialPage) {
        return TestAttribute.INITIAL_PAGE.set(initialPage);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getOutputDirectory() {
        ITestResult testResult = Reporter.getCurrentTestResult();
        if (testResult != null) {
            return testResult.getTestContext().getOutputDirectory();
        } else {
            return PathUtils.ReportsDirectory.ARTIFACT.getPath().toString();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void adjustTimeout(final long adjust) {
        ITestResult testResult = Reporter.getCurrentTestResult();
        if (testResult != null) {
            long timeout = testResult.getMethod().getTimeOut();
            if (timeout > 0) {
                testResult.getMethod().setTimeOut(timeout + adjust);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTest(final Method method) {
        return null != method.getAnnotation(Test.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBeforeMethod(final Method method) {
        return null != method.getAnnotation(BeforeMethod.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAfterMethod(final Method method) {
        return null != method.getAnnotation(AfterMethod.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBeforeClass(final Method method) {
        return null != method.getAnnotation(BeforeClass.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAfterClass(final Method method) {
        return null != method.getAnnotation(AfterClass.class);
    }
    
    /**
     * Get the listener of the specified type that's attached to the listener chain.
     * 
     * @param <T> listener type
     * @param listenerType listener type
     * @return listener of the specified type
     */
    public static <T extends ITestNGListener> T getLinkedListener(final Class<T> listenerType) {
        ITestResult testResult = Reporter.getCurrentTestResult();
        Optional<T> optListener = 
                        ListenerChain.getAttachedListener(testResult, listenerType);
        if (optListener.isPresent()) {
            return optListener.get();
        }
        throw new IllegalStateException(listenerType.getSimpleName() + " listener wasn't found on the listener chain");
    }
}
