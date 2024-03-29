package com.nordstrom.automation.selenium.plugins;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.exceptions.DriverExecutableNotFoundException;
import com.nordstrom.automation.selenium.utility.BinaryFinder;

public class SafariCaps {
    
    private SafariCaps() {
        throw new AssertionError("SafariCaps is a static constants class that cannot be instantiated");
    }

    public static final String DRIVER_NAME = "safari";
    public static final String DRIVER_PATH = "webdriver.safari.driver";
    public static final String BINARY_PATH = "webdriver.safari.bin";
    public static final String OPTIONS_KEY = "safari.options";
    private static final String[] PROPERTY_NAMES = { DRIVER_PATH, BINARY_PATH, "webdriver.safari.noinstall" };

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
    
    public static String getCapabilities() {
        return CAPABILITIES;
    }

    public static Map<String, String> getPersonalities() {
        return PERSONALITIES;
    }

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
