package com.nordstrom.automation.selenium;

public interface DriverPlugin {

    /**
     * Get fully-qualified names of context classes for Selenium Grid dependencies.
     * 
     * @return context class names for Selenium Grid dependencies
     */
    public String[] getDependencyContexts();
    
    /**
     * Get the name by which this browser is known to Selenium Grid.
     * 
     * @return Selenium Grid browser identifier
     */
    public String getBrowserName();
}
