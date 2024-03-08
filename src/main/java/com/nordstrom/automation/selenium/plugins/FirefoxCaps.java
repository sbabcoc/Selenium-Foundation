package com.nordstrom.automation.selenium.plugins;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.exceptions.DriverExecutableNotFoundException;
import com.nordstrom.automation.selenium.utility.BinaryFinder;

public class FirefoxCaps {
    
    private FirefoxCaps() {
        throw new AssertionError("FirefoxCaps is a static constants class that cannot be instantiated");
    }

    public static final String DRIVER_NAME = "firefox";
    public static final String DRIVER_PATH = "webdriver.gecko.driver";
    public static final String BINARY_PATH = "webdriver.firefox.bin";
    public static final String LOGFILE_PATH = "webdriver.firefox.logfile";
    public static final String PROFILE_PATH = "webdriver.firefox.profile";
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
