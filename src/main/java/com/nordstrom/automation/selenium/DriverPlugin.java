package com.nordstrom.automation.selenium;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.openqa.grid.common.GridRole;

import com.nordstrom.automation.selenium.core.SeleniumGrid;
import com.nordstrom.automation.selenium.core.SeleniumGrid.GridServer;

public interface DriverPlugin {
    
    String[] getDependencyContexts();
    
    String getCapabilities();
    
    /**
     * 
     * @param launcherClassName
     * @param dependencyContexts
     * @param nodeConfigPath
     * @return
     */
    public default GridServer start(String launcherClassName, String[] dependencyContexts, Path nodeConfigPath) {
        String[] combinedContexts = combineDependencyContexts(dependencyContexts);
        return SeleniumGrid.start(launcherClassName, combinedContexts, GridRole.NODE, Integer.valueOf(-1), nodeConfigPath);
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
