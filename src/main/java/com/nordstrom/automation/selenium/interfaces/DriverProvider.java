package com.nordstrom.automation.selenium.interfaces;

import java.lang.reflect.Method;

import org.openqa.selenium.WebDriver;
import com.nordstrom.automation.selenium.support.TestBase;

/**
 * Test classes with non-standard driver configurations implement this interface, which enables the driver manager 
 * to obtain a driver from the {@link #provideDriver(TestBase, Method)} method of test class instance.
 */
public interface DriverProvider {
    
    /**
     * Acquire a driver object for the specified method.
     * 
     * @param instance test class instance
     * @param method the method being invoked
     * @return driver object
     */
    WebDriver provideDriver(TestBase instance, Method method);

}
