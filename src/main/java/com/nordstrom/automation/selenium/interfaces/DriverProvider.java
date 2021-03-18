package com.nordstrom.automation.selenium.interfaces;

import java.lang.reflect.Method;

import org.openqa.selenium.WebDriver;

/**
 * Test classes with non-standard driver configurations implement this interface, which enables the driver manager 
 * to obtain a driver from the {@link #provideDriver(Method)} method of test class instance.
 */
public interface DriverProvider {
    
    /**
     * Acquire a driver object for the specified method.
     * @param method the method being invoked
     * 
     * @return driver object
     */
    WebDriver provideDriver(Method method);

}
