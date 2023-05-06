package com.nordstrom.automation.selenium.plugins;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.nordstrom.automation.selenium.exceptions.DriverExecutableNotFoundException;
import com.nordstrom.automation.selenium.utility.BinaryFinder;

public class ChromeCaps {
    
    private ChromeCaps() {
        throw new AssertionError("ChromeCaps is a static constants class that cannot be instantiated");
    }

    public static final String DRIVER_NAME = "chrome";
    public static final String DRIVER_PATH = "webdriver.chrome.driver";
    public static final String BINARY_PATH = "webdriver.chrome.bin";
    public static final String LOGFILE_PATH = "webdriver.chrome.logfile";
    public static final String VERBOSE_LOG = "webdriver.chrome.verboseLogging";
    public static final String SILENT_MODE = "webdriver.chrome.silentOutput";
    public static final String WHITELISTED = "webdriver.chrome.whitelistedIps";
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
