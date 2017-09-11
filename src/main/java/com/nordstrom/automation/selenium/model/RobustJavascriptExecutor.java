package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.internal.WrapsDriver;

import com.nordstrom.automation.selenium.core.WebDriverUtils;

/**
 * This is a wrapper class for drivers that implement the {@link JavascriptExecutor} interface. If a 
 * {@link StaleElementReferenceException} failure is encountered on invocation of {@link #executeScript} or 
 * {@link #executeAsyncScript}, the specified arguments array will be scanned for {@link RobustWebElement} 
 * objects. If any such objects are found, the element references wrapped by these objects are refreshed 
 * and execution of the specified script is tried again.
 * <p>
 * To obtain a {@link RobustJavascriptExecutor} object, use the {@link WebDriverUtils#getExecutor} method.
 * <p>
 * <b>NOTE</b>: Any element references obtained by executed scripts are returned unwrapped. There is no 
 * generic way to wrap such references in {@link RobustWebElement} objects. It is recommended that you 
 * wrap such 'native' references yourself to maintain the stability of your automation.
 * 
 * @see RobustWebElement
 * @see WebDriverUtils#getExecutor
 */
public class RobustJavascriptExecutor implements JavascriptExecutor, WrapsDriver {
    
    private JavascriptExecutor executor;
    
    /**
     * Constructor for robust JavaScript executor
     * 
     * @param driver driver object
     */
    public RobustJavascriptExecutor(WebDriver driver) {
        if (driver instanceof JavascriptExecutor) {
            executor = (JavascriptExecutor) driver;
        } else {
            throw new UnsupportedOperationException("The specified driver is unable to execute JavaScript");
        }
    }

    @Override
    public Object executeAsyncScript(String script, Object... args) {
        Object result = null;
        try {
            result = executor.executeAsyncScript(script, args);
        } catch (StaleElementReferenceException e) {
            if (refreshReferences(e, args)) {
                executeAsyncScript(script, args);
            } else {
                throw e;
            }
        }
        return result;
    }

    @Override
    public Object executeScript(String script, Object... args) {
        Object result = null;
        try {
            result = executor.executeScript(script, args);
        } catch (StaleElementReferenceException e) {
            if (refreshReferences(e, args)) {
                executeScript(script, args);
            } else {
                throw e;
            }
        }
        return result;
    }

    @Override
    public WebDriver getWrappedDriver() {
        return (WebDriver) executor;
    }
    
    /**
     * Refresh references wrapped by {@link RobustWebElement} objects in the specified arguments array.
     * 
     * @param e {@link StaleElementReferenceException} that prompted this refresh
     * @param args arguments array to scan for {@link RobustWebElement} objects
     * @return 'true' if at least one {@link RobustWebElement} object was refreshed; otherwise 'false'
     */
    private static boolean refreshReferences(StaleElementReferenceException e, Object... args) {
        boolean didRefresh = false;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof RobustWebElement) {
                ((RobustWebElement) args[i]).refreshReference(e);
                didRefresh = true;
            }
        }
        
        return didRefresh;
    }
    
}
