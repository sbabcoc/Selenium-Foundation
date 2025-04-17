package com.nordstrom.automation.selenium;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.core.LocalSeleniumGrid.LocalGridServer;
import net.bytebuddy.implementation.Implementation;

/**
 * This interface defines the contract for driver plug-in objects.
 */
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
     * @param capabilities required capabilities for target driver
     * @return System property names
     */
    String[] getPropertyNames(String capabilities);
    
    /**
     * Start local Selenium Grid node for this driver.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubUrl Grid hub {@link URL} with which node should register
     * @return {@link LocalGridServer} object for specified node
     * @throws IOException if an I/O error occurs
     */
    default LocalGridServer create(SeleniumConfig config, URL hubUrl) throws IOException {
        String launcherClassName = config.getString(SeleniumSettings.GRID_LAUNCHER.key());
        String[] dependencyContexts = config.getDependencyContexts();
        String workingDir = config.getString(SeleniumSettings.GRID_WORKING_DIR.key());
        Path workingPath = (workingDir == null || workingDir.isEmpty()) ? null : Paths.get(workingDir);
        return create(config, launcherClassName, dependencyContexts, hubUrl, workingPath);
    }

    /**
     * Start local Selenium Grid node for this driver.
     * 
     * @param config {@link SeleniumConfig} object
     * @param launcherClassName fully-qualified name of {@code GridLauncher} class
     * @param dependencyContexts fully-qualified names of context classes for Selenium Grid dependencies
     * @param hubUrl Grid hub {@link URL} with which node should register
     * @param workingPath {@link Path} of working directory for server process; {@code null} for default
     * @return {@link LocalGridServer} object for specified node
     * @throws IOException if an I/O error occurs
     */
    default LocalGridServer create(SeleniumConfig config, String launcherClassName, String[] dependencyContexts,
            URL hubUrl, Path workingPath) throws IOException {
        
        Path outputPath = GridUtility.getOutputPath(config, false);
        LocalGridServer nodeServer = 
                create(config, launcherClassName, dependencyContexts, hubUrl, workingPath, outputPath);
        nodeServer.getPersonalities().putAll(getPersonalities());
        return nodeServer;
    }

    /**
     * Start local Selenium Grid node for this driver.
     * 
     * @param config {@link SeleniumConfig} object
     * @param launcherClassName fully-qualified class name for Grid launcher
     * @param dependencyContexts common dependency contexts for all Grid nodes
     * @param hubUrl Grid hub {@link URL} with which node should register
     * @param workingPath {@link Path} of working directory for server process; {@code null} for default
     * @param outputPath {@link Path} to output log file; {@code null} to decline log-to-file
     * @return {@link LocalGridServer} object for specified node
     * @throws IOException if an I/O error occurs
     */
    LocalGridServer create(SeleniumConfig config, String launcherClassName, String[] dependencyContexts,
            URL hubUrl, final Path workingPath, final Path outputPath) throws IOException;
    
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
