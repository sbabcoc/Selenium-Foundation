package com.nordstrom.automation.selenium;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.Map;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.nordstrom.automation.selenium.core.LocalSeleniumGrid.LocalGridServer;
import com.nordstrom.automation.selenium.core.SeleniumGrid.GridServer;

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
     * Start local Selenium Grid node for this driver.
     * 
     * @param config {@link SeleniumConfig} object
     * @param launcherClassName fully-qualified class name for Grid launcher
     * @param dependencyContexts common dependency contexts for all Grid nodes
     * @param hubServer Grid hub server with which node should register
     * @param workingPath {@link Path} of working directory for server process; {@code null} for default
     * @param outputPath {@link Path} to output log file; {@code null} to decline log-to-file
     * @return {@link LocalGridServer} object for specified node
     * @throws IOException if an I/O error occurs
     */
    LocalGridServer create(SeleniumConfig config, String launcherClassName, String[] dependencyContexts,
            GridServer hubServer, final Path workingPath, final Path outputPath) throws IOException;
    
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
