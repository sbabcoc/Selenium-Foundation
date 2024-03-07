package com.nordstrom.automation.selenium.plugins;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.exceptions.DriverExecutableNotFoundException;
import com.nordstrom.automation.selenium.utility.BinaryFinder;

public class PhantomJsCaps {
    
    private PhantomJsCaps() {
        throw new AssertionError("PhantomJsCaps is a static constants class that cannot be instantiated");
    }

    public static final String DRIVER_NAME = "phantomjs";
    public static final String DRIVER_PATH = "phantomjs.binary.path";
    private static final String[] PROPERTY_NAMES = 
        { DRIVER_PATH, "phantomjs.ghostdriver.path", "phantomjs.logfile.path" };

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
    
    public static String getCapabilities() {
        return CAPABILITIES;
    }

    public static Map<String, String> getPersonalities() {
        return PERSONALITIES;
    }
    
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
