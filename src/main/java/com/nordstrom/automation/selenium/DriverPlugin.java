package com.nordstrom.automation.selenium;

import java.util.Map;

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
    
}
