package com.nordstrom.automation.selenium.plugins;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.exceptions.DriverExecutableNotFoundException;
import com.nordstrom.automation.selenium.utility.BinaryFinder;

/**
 * This class defines properties and methods used by plug-ins that support the Apple Safari browser.
 */
public class SafariCaps {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private SafariCaps() {
        throw new AssertionError("SafariCaps is a static constants class that cannot be instantiated");
    }

    /** driver name */
    public static final String DRIVER_NAME = "safari";
    /** driver path system property */
    public static final String DRIVER_PATH = "webdriver.safari.driver";
    /** browser binary path system property */
    public static final String BINARY_PATH = "webdriver.safari.bin";
    /** (legacy} skip installation system property */
    public static final String NO_INSTALL = "webdriver.safari.noinstall";
    /** extension capability name for <b>SafariOptions</b> */
    public static final String OPTIONS_KEY = "safari.options";
    
    private static final String[] PROPERTY_NAMES = { DRIVER_PATH, BINARY_PATH, NO_INSTALL };

    private static final String CAPABILITIES =
            "{\"browserName\":\"safari\"}";
    
    private static final String BASELINE = 
            "{\"browserName\":\"safari\"," +
             "\"nord:options\":{\"personality\":\"safari\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.SafariPlugin\"}}";
    
    private static final Map<String, String> PERSONALITIES;
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(DRIVER_NAME, BASELINE);
        PERSONALITIES = Collections.unmodifiableMap(personalities);
    }
    
    /**
     * Get capabilities supported by this plug-in.
     * 
     * @return core {@link org.openqa.selenium.Capabilities Capabilities} as JSON object
     */
    public static String getCapabilities() {
        return CAPABILITIES;
    }

    /**
     * Get browser "personalities" provided by this plug-in.
     * 
     * @return map of JSON {@link org.openqa.selenium.Capabilities Capabilities} objects keyed by "personality" name
     */
    public static Map<String, String> getPersonalities() {
        return PERSONALITIES;
    }

    /**
     * Get list of system property names recognized by the driver associated with this plug-in.
     * <p>
     * <b>NOTE</b>: As a side-effect, this method invokes <b>WebDriverManager</b> (Selenium 3) or
     * <b>SeleniumManager</b> (Selenium 4) to acquire the driver path. If a driver that supports the
     * browser corresponding to the specified capabilities is not found, the manager will attempt to
     * install one. If the manager acquires the driver path, this method stores it in the associated
     * system property.
     * 
     * @param capabilities JSON {@link org.openqa.selenium.Capabilities Capabilities} object
     * @return list of system property names
     */
    public static String[] getPropertyNames(String capabilities) {
        try {
            File driverPath = BinaryFinder.findDriver(capabilities);
            System.setProperty(DRIVER_PATH, driverPath.getAbsolutePath());
        } catch (IllegalStateException e) {
            throw new DriverExecutableNotFoundException(DRIVER_PATH);
        }
        return PROPERTY_NAMES;
    }

}
