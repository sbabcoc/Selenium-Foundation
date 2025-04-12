package com.nordstrom.automation.selenium.plugins;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.exceptions.DriverExecutableNotFoundException;
import com.nordstrom.automation.selenium.utility.BinaryFinder;

/**
 * This class defines properties and methods used by plug-ins that support the PhantomJS browser.
 */
public class PhantomJsCaps {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private PhantomJsCaps() {
        throw new AssertionError("PhantomJsCaps is a static constants class that cannot be instantiated");
    }

    /** driver name */
    public static final String DRIVER_NAME = "phantomjs";
    /** driver path system property */
    public static final String DRIVER_PATH = "phantomjs.binary.path";
    /** browser binary path system property */
    public static final String BINARY_PATH = "phantomjs.ghostdriver.path";
    /** log file path system property */
    public static final String LOGFILE_PATH = "phantomjs.logfile.path";
    
    private static final String[] PROPERTY_NAMES = 
        { DRIVER_PATH, BINARY_PATH, LOGFILE_PATH };

    private static final String CAPABILITIES =
            "{\"browserName\":\"phantomjs\"}";
    
    private static final String BASELINE =
            "{\"browserName\":\"phantomjs\"," +
             "\"nord:options\":{\"personality\":\"phantomjs\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.PhantomJsPlugin\"}}";
    
    private static final String LOGGING = 
            "{\"browserName\":\"phantomjs\",\"loggingPrefs\":{\"browser\":\"WARNING\"}," +
             "\"nord:options\":{\"personality\":\"phantomjs.logging\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.PhantomJsPlugin\"}}";
    
    private static final Map<String, String> PERSONALITIES;
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(DRIVER_NAME, BASELINE);
        personalities.put(DRIVER_NAME + ".logging", LOGGING);
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
     * <b>NOTE</b>: As a side-effect, this method invokes the {@link BinaryFinder#findBinary findBinary}
     * method to locate the driver. If the driver is found, this method stores its path in the associated
     * system property.
     * 
     * @param capabilities JSON {@link org.openqa.selenium.Capabilities Capabilities} object
     * @return list of system property names
     */
    public static String[] getPropertyNames(String capabilities) {
        try {
            File driverPath = BinaryFinder.findBinary(DRIVER_NAME, DRIVER_PATH);
            System.setProperty(DRIVER_PATH, driverPath.getAbsolutePath());
        } catch (IllegalStateException e) {
            throw new DriverExecutableNotFoundException(DRIVER_PATH);
        }
        return PROPERTY_NAMES;
    }

}
