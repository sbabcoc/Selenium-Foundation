package com.nordstrom.automation.selenium;

import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import net.bytebuddy.implementation.Implementation;

public interface DriverPlugin {
    
    /**
     * Get dependency contexts for this driver.
     * 
     * @return driver dependency contexts
     */
    String[] getDependencyContexts();
    
    /**
     * Get driver capabilities as JSON string.
     * 
     * @param config {@link SeleniumConfig} object
     * @return JSON driver capabilities
     */
    String getCapabilities(SeleniumConfig config);
    
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
     * Get names of supported System properties.
     * 
     * @return System property names
     */
    String[] getPropertyNames();
    
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
