package com.nordstrom.automation.selenium;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.openqa.grid.common.GridRole;

import com.nordstrom.automation.selenium.core.LocalSeleniumGrid;
import com.nordstrom.automation.selenium.core.LocalSeleniumGrid.LocalGridServer;

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
     * @return JSON driver capabilities
     */
    String getCapabilities();
    
    /**
     * Start local Selenium Grid node for this driver.
     * 
     * @param launcherClassName fully-qualified class name for Grid launcher
     * @param dependencyContexts common dependency contexts for all Grid nodes
     * @param nodeConfigPath {@link Path} to Grid node configuration file
     * @return {@link LocalGridServer} object for specified node
     */
    public default LocalGridServer start(String launcherClassName, String[] dependencyContexts, Path nodeConfigPath) {
        String[] combinedContexts = combineDependencyContexts(dependencyContexts);
        return LocalSeleniumGrid.start(launcherClassName, combinedContexts, GridRole.NODE, Integer.valueOf(-1), nodeConfigPath);
    }
    
    /**
     * Combine driver dependency contexts with the specified core Selenium Grid contexts.
     *
     * @param dependencyContexts core Selenium Grid dependency contexts
     * @return combined contexts for Selenium Grid dependencies
     */
    public default String[] combineDependencyContexts(String[] dependencyContexts) {
        return Stream.concat(Stream.of(dependencyContexts), Stream.of(getDependencyContexts())).toArray(String[]::new);
    }
    
}
