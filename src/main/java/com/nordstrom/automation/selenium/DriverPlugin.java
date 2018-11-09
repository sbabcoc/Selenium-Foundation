package com.nordstrom.automation.selenium;

import java.util.List;

import org.openqa.selenium.Capabilities;
import com.nordstrom.automation.selenium.core.GridProcess;

public interface DriverPlugin {

    /**
     * Get fully-qualified names of context classes for Selenium Grid dependencies.
     * 
     * @return context class names for Selenium Grid dependencies
     */
    public String[] getDependencyContexts();
    
    public List<Capabilities> getCapabilitiesList();
    
    public default GridProcess.GridServer launchGridNode() {
        Process process = GridProcess.start(null, null, null);
        return null;
    }
}
