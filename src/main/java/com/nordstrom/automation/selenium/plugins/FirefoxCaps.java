package com.nordstrom.automation.selenium.plugins;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.exceptions.DriverExecutableNotFoundException;
import com.nordstrom.automation.selenium.utility.BinaryFinder;

/**
 * This class defines properties and methods used by plug-ins that support the Mozilla Firefox browser.
 */
public class FirefoxCaps {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private FirefoxCaps() {
        throw new AssertionError("FirefoxCaps is a static constants class that cannot be instantiated");
    }

    /** driver name */
    public static final String DRIVER_NAME = "firefox";
    /** driver path system property */
    public static final String DRIVER_PATH = "webdriver.gecko.driver";
    /** browser binary path system property */
    public static final String BINARY_PATH = "webdriver.firefox.bin";
    /** log file path system property */
    public static final String LOGFILE_PATH = "webdriver.firefox.logfile";
    /** profile path system property */
    public static final String PROFILE_PATH = "webdriver.firefox.profile";
    /** extension capability name for <b>FirefoxOptions</b> */
    public static final String OPTIONS_KEY = "moz:firefoxOptions";
    
    private static final String[] PROPERTY_NAMES =
        { DRIVER_PATH, BINARY_PATH, LOGFILE_PATH, PROFILE_PATH };
    
    private static final String CAPABILITIES =
            "{\"browserName\":\"firefox\"}";
    
    private static final String BASELINE =
            "{\"browserName\":\"firefox\"," +
             "\"nord:options\":{\"personality\":\"firefox\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.FirefoxPlugin\"}}";
    
    private static final String HEADLESS =
            "{\"browserName\":\"firefox\"," +
             "\"moz:firefoxOptions\":{\"args\":[\"-headless\"]}," +
             "\"nord:options\":{\"personality\":\"firefox.headless\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.FirefoxPlugin\"}}";
    
    private static final Map<String, String> PERSONALITIES;
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(DRIVER_NAME, BASELINE);
        personalities.put(DRIVER_NAME + ".headless", HEADLESS);
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
            Path driverPath = BinaryFinder.findDriver(capabilities);
            System.setProperty(DRIVER_PATH, driverPath.toAbsolutePath().toString());
        } catch (IllegalStateException e) {
            throw new DriverExecutableNotFoundException(DRIVER_PATH);
        }
        return PROPERTY_NAMES;
    }

}
