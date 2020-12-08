package com.nordstrom.automation.selenium.plugins;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.LocalSeleniumGrid.LocalGridServer;
import com.nordstrom.automation.selenium.core.SeleniumGrid.GridServer;

import net.bytebuddy.implementation.Implementation;

public class AppiumPlugin implements DriverPlugin {

    private static final String[] DEPENDENCY_CONTEXTS = {};
    
    @Override
    public String[] getDependencyContexts() {
        return DEPENDENCY_CONTEXTS;
    }

    @Override
    public String getCapabilitiesForDriver(SeleniumConfig config, String driverName) {
        return AppiumCaps.getCapabilities(driverName);
    }

    @Override
    public String[] getDriverNames() {
        return AppiumCaps.DRIVER_NAMES;
    }

    @Override
    public Map<String, String> getPersonalitiesForDriver(String driverName) {
        return AppiumCaps.getPersonalities();
    }

    @Override
    public String[] getPropertyNames() {
        return AppiumCaps.getPropertyNames();
    }

    @Override
    public LocalGridServer start(SeleniumConfig config, String launcherClassName, String[] dependencyContexts,
            GridServer hubServer, Path workingPath, Path outputPath) throws IOException {
        // TODO Auto-generated method stub
        // Create subclass of LocalGridServer.
        // Override default status request string and shutdown(...) method
        return null;
    }

    @Override
    public Implementation getWebElementCtor(WebDriver driver, Class<? extends WebElement> refClass) {
        // TODO Auto-generated method stub
        return null;
    }

}
