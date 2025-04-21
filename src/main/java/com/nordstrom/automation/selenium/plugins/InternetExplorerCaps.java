package com.nordstrom.automation.selenium.plugins;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.exceptions.DriverExecutableNotFoundException;
import com.nordstrom.automation.selenium.utility.BinaryFinder;

/**
 * This class defines properties and methods used by plug-ins that support the Microsoft Internet Explorer browser.
 */
public class InternetExplorerCaps {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private InternetExplorerCaps() {
        throw new AssertionError("InternetExplorerCaps is a static constants class that cannot be instantiated");
    }

    /** driver name */
    public static final String DRIVER_NAME = "internet explorer";
    /** driver path system property */
    public static final String DRIVER_PATH = "webdriver.ie.driver";
    /** log file path system property */
    public static final String LOGFILE_PATH = "webdriver.ie.driver.logfile";
    /** log level system property */
    public static final String LOG_LEVEL = "webdriver.ie.driver.loglevel";
    /** driver host system property */
    public static final String DRIVER_HOST = "webdriver.ie.driver.host";
    /** extraction path system property */
    public static final String EXTRACT_PATH = "webdriver.ie.driver.extractpath";
    /** "silent mode" system property */
    public static final String SILENT_MODE = "webdriver.ie.driver.silent";
    /** extension capability name for <b>InternetExporerOptions</b> */
    public static final String OPTIONS_KEY = "se:ieOptions";
    
    private static final String[] PROPERTY_NAMES = 
        { DRIVER_PATH, LOGFILE_PATH, LOG_LEVEL, DRIVER_HOST, EXTRACT_PATH, SILENT_MODE };
    
    private static final String CAPABILITIES =
            "{\"browserName\":\"internet explorer\"}";
    
    private static final String BASELINE =
            "{\"browserName\":\"internet explorer\"," +
             "\"nord:options\":{\"personality\":\"internet explorer\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.InternetExplorerPlugin\"}}";
    
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
