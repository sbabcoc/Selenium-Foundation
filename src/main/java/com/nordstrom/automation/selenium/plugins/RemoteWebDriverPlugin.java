package com.nordstrom.automation.selenium.plugins;

import java.io.IOException;
import java.nio.file.Path;

import org.openqa.grid.common.GridRole;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.LocalSeleniumGrid;
import com.nordstrom.automation.selenium.core.LocalSeleniumGrid.LocalGridServer;
import com.nordstrom.automation.selenium.core.SeleniumGrid.GridServer;

import net.bytebuddy.implementation.Implementation;

/**
 * This class provides the base plugin implementation for drivers that extent {@code RemoteWebDriver}.
 */
public abstract class RemoteWebDriverPlugin implements DriverPlugin {
    
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
	@Override
	public LocalGridServer start(SeleniumConfig config, String launcherClassName, String[] dependencyContexts,
			GridServer hubServer, final Path workingPath, final Path outputPath) throws IOException {
    	
        String[] combinedContexts = LocalSeleniumGrid.combineDependencyContexts(dependencyContexts, this);
        Path nodeConfigPath = config.createNodeConfig(getCapabilities(config), hubServer.getUrl());
        String[] propertyNames = getPropertyNames();
        return LocalSeleniumGrid.start(launcherClassName, combinedContexts, GridRole.NODE,
                Integer.valueOf(-1), nodeConfigPath, workingPath, outputPath, propertyNames);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Implementation getWebElementCtor(WebDriver driver, Class<? extends WebElement> refClass) {
        return null;
    }
}
