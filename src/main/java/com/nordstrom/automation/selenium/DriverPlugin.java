package com.nordstrom.automation.selenium;

import java.util.Map;

import org.openqa.selenium.Capabilities;
import com.nordstrom.automation.selenium.core.GridProcess;
import com.nordstrom.automation.selenium.core.GridProcess.GridServer;

public interface DriverPlugin {

    /**
     * Get fully-qualified names of context classes for Selenium Grid dependencies.
     * 
     * @return context class names for Selenium Grid dependencies
     */
    public String[] getDependencyContexts();
    
    public Map<String, Capabilities> getCapabilitiesMap();
    
    public default GridServer launchGridNode() {
        return GridProcess.start(null, null, null);
    }
}
