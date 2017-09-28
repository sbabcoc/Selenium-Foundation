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

@LinkedListeners({ScreenshotCapture.class, DriverManager.class, ExecutionFlowController.class})
public abstract class TestNGBase implements TestBase {
    
    private enum TestAttributes {
        DRIVER("Driver"),
        INITIAL_PAGE("InitialPage");
        
        private String key;
        
        TestAttributes(String key) {
            this.key = key;
        }
        
        private Optional<?> set(Object obj) {
            Optional<?> val;
            if (obj != null) {
                val = Optional.of(obj);
            } else {
                val = Optional.empty();
            }
            ITestResult result = Reporter.getCurrentTestResult();
            result.setAttribute(key, val);
            return val;
        }
        
        private Optional<?> find() {
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
    public Optional<WebDriver> findDriver() {
        return (Optional<WebDriver>) TestAttributes.DRIVER.find();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<WebDriver> setDriver(WebDriver driver) {
        return (Optional<WebDriver>) TestAttributes.DRIVER.set(driver);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Page> findInitialPage() {
        return (Optional<Page>) TestAttributes.INITIAL_PAGE.find();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Page> setInitialPage(Page initialPage) {
        return (Optional<Page>) TestAttributes.INITIAL_PAGE.set(initialPage);
    }
}
