package com.nordstrom.automation.selenium;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import org.openqa.grid.common.GridRole;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.LocalSeleniumGrid;
import com.nordstrom.automation.selenium.core.LocalSeleniumGrid.LocalGridServer;
import com.nordstrom.automation.selenium.core.SeleniumGrid.GridServer;

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
     * Get driver "personalities" provided by this plug-in.
     * 
     * @return named collection of capabilities records
     */
    Map<String, String> getPersonalities();
    
    /**
     * Start local Selenium Grid node for this driver.
     * 
     * @param config {@link SeleniumConfig} object
     * @param launcherClassName fully-qualified class name for Grid launcher
     * @param dependencyContexts common dependency contexts for all Grid nodes
     * @param hubServer Grid hub server with which node should register
     * @return {@link LocalGridServer} object for specified node
     * @throws IOException if an I/O error occurs
     */
    public default LocalGridServer start(SeleniumConfig config, String launcherClassName,
                    String[] dependencyContexts, GridServer hubServer) throws IOException {

        String[] combinedContexts = combineDependencyContexts(dependencyContexts);
        Path nodeConfigPath = config.createNodeConfig(getCapabilities(), hubServer.getUrl());
        return LocalSeleniumGrid.start(launcherClassName, combinedContexts, GridRole.NODE,
                        Integer.valueOf(-1), nodeConfigPath);
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
