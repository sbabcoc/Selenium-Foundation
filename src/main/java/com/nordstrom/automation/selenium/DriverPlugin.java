package com.nordstrom.automation.selenium;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import net.bytebuddy.implementation.Implementation;

/**
 * This interface defines the contract for driver plug-in objects.
 */
public interface DriverPlugin {
    
    /**
     * Get name of browser supported by this plug-in.
     * 
     * @return browser name
     */
    String getBrowserName();
    
    /**
     * Get driver "personalities" provided by this plug-in.
     * 
     * @return named collection of capabilities records
     */
    Map<String, String> getPersonalities();
    
    /**
     * Get constructor for this driver's {@link RemoteWebDriver} implementation.
     * <p>
     * <b>NOTE</b>: This is only needed for implementations that require driver-specific implementation.
     * 
     * @param <T> constructor type parameter
     * @param desiredCapabilities desired capabilities for the driver
     * @return constructor for driver-specific {@link RemoteWebDriver} implementation
     */
    <T extends RemoteWebDriver> Constructor<T> getRemoteWebDriverCtor(Capabilities desiredCapabilities);
    
    /**
     * Get default constructor for this driver's {@link WebElement} implementation.
     * <p>
     * <b>NOTE</b>: This is only needed for implementations that use non-default constructors.
     * 
     * @param driver target driver instance
     * @param refClass class of {@code WebDriver} implementation
     * @return default constructor implementation
     */
    Implementation getWebElementCtor(WebDriver driver, Class<? extends WebElement> refClass);
    
}
