package com.nordstrom.automation.selenium.junit;

import java.lang.reflect.Method;

import org.junit.rules.ExternalResource;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.nordstrom.automation.junit.JUnitMethodWatcher;
import com.nordstrom.automation.selenium.annotations.PageUrl;
import com.nordstrom.automation.selenium.core.DriverManager;
import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.core.TestBase;

/**
 * This JUnit watcher performs several basic functions related to driver session management:
 * <ul>
 *     <li>Manage Selenium driver lifetime.</li>
 *     <li>For local execution, manage a local instance of Selenium Grid.</li>
 *     <li>Store and dispense the driver instance created for the test.</li>
 *     <li>Manage configured driver timeout intervals.</li>
 *     <li>If an initial page class is specified:
 *         <ul>
 *             <li>Open the initial page based on its {@link PageUrl} annotation.</li>
 *             <li>Store the page object for subsequent dispensing to the test.</li>
 *         </ul>
 *     </li>
 * </ul>
 * 
 * @see GridUtility
 */
public class DriverWatcher implements JUnitMethodWatcher {

    @Override
    public void beforeInvocation(Object obj, Method method, Object[] args) {
        DriverManager.beforeInvocation(obj, method);
    }

    @Override
    public void afterInvocation(Object obj, Method method, Object[] args) {
        DriverManager.afterInvocation(obj, method);
    }
    
    /**
     * Get test watcher to manage driver instances.
     * 
     * @param obj test class instance extending {@link TestBase}
     * @return test watcher object
     */
    public static TestWatcher getTestWatcher(final TestBase obj) {
        return new TestWatcher() {
            @Override
            protected void finished(Description description) {
                DriverManager.closeDriver(obj);
            }
        };
    }
    
    /**
     * Get class watcher to manage local Grid servers.
     * 
     * @return external resource object
     */
    public static ExternalResource getClassWatcher() {
        return new ExternalResource() {
            @Override
            protected void after() {
                DriverManager.onFinish();
            }
            
        };
    }
}
