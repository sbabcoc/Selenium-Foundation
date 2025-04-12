package com.nordstrom.automation.selenium.plugins;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.nordstrom.automation.selenium.exceptions.DriverExecutableNotFoundException;
import com.nordstrom.automation.selenium.utility.BinaryFinder;

/**
 * This class defines properties and methods used by plug-ins that support the Google Chrome browser.
 */
public class ChromeCaps {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private ChromeCaps() {
        throw new AssertionError("ChromeCaps is a static constants class that cannot be instantiated");
    }

    /** driver name */
    public static final String DRIVER_NAME = "chrome";
    /** driver path system property */
    public static final String DRIVER_PATH = "webdriver.chrome.driver";
    /** browser binary path system property */
    public static final String BINARY_PATH = "webdriver.chrome.bin";
    /** log file path system property */
    public static final String LOGFILE_PATH = "webdriver.chrome.logfile";
    /** verbose logging system property */
    public static final String VERBOSE_LOG = "webdriver.chrome.verboseLogging";
    /** "silent mode" system property */
    public static final String SILENT_MODE = "webdriver.chrome.silentOutput";
    /** white-listed IP addresses system property */
    public static final String WHITELISTED = "webdriver.chrome.whitelistedIps";
    /** extension capability name for <b>ChromeOptions</b> */
    public static final String OPTIONS_KEY = "goog:chromeOptions";
    
    private static final String[] PROPERTY_NAMES = 
        { DRIVER_PATH, BINARY_PATH, LOGFILE_PATH, VERBOSE_LOG, SILENT_MODE, WHITELISTED };
    
    private static final String CAPABILITIES =
            "{\"browserName\":\"chrome\"}";

    private static final String BASELINE =
            "{\"browserName\":\"chrome\"," +
             "\"goog:chromeOptions\":{\"args\":[\"--disable-infobars\"]," +
                                     "\"prefs\":{\"credentials_enable_service\":false}}," +
             "\"nord:options\":{\"personality\":\"chrome\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.ChromePlugin\"}}";
    
    private static final String HEADLESS =
            "{\"browserName\":\"chrome\"," +
             "\"goog:chromeOptions\":{\"args\":[\"--disable-infobars\",\"--headless\",\"--disable-gpu\"]," +
                                     "\"prefs\":{\"credentials_enable_service\":false}}," +
             "\"nord:options\":{\"personality\":\"chrome.headless\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.ChromePlugin\"}}";
    
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
            File driverPath = BinaryFinder.findDriver(capabilities);
            System.setProperty(DRIVER_PATH, driverPath.getAbsolutePath());
        } catch (IllegalStateException e) {
            throw new DriverExecutableNotFoundException(DRIVER_PATH);
        }
        return PROPERTY_NAMES;
    }

}
