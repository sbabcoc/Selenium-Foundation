package com.nordstrom.automation.selenium.plugins;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.exceptions.DriverExecutableNotFoundException;
import com.nordstrom.automation.selenium.utility.BinaryFinder;

public class InternetExplorerCaps {
    
    private InternetExplorerCaps() {
        throw new AssertionError("InternetExplorerCaps is a static constants class that cannot be instantiated");
    }

    public static final String DRIVER_NAME = "internet explorer";
    public static final String DRIVER_PATH = "webdriver.ie.driver";
    public static final String LOGFILE_PATH = "webdriver.ie.driver.logfile";
    public static final String LOG_LEVEL = "webdriver.ie.driver.loglevel";
    public static final String DRIVER_HOST = "webdriver.ie.driver.host";
    public static final String EXTRACT_PATH = "webdriver.ie.driver.extractpath";
    public static final String SILENT_MODE = "webdriver.ie.driver.silent";
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
