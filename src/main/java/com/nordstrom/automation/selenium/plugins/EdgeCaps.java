package com.nordstrom.automation.selenium.plugins;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.exceptions.DriverExecutableNotFoundException;
import com.nordstrom.automation.selenium.utility.BinaryFinder;

public class EdgeCaps {
    
    private EdgeCaps() {
        throw new AssertionError("EdgeCaps is a static constants class that cannot be instantiated");
    }

    public static final String DRIVER_NAME = "MicrosoftEdge";
    public static final String DRIVER_PATH = "webdriver.edge.driver";
    public static final String BINARY_PATH = "webdriver.edge.bin";
    public static final String LOGFILE_PATH = "webdriver.edge.logfile";
    public static final String VERBOSE_LOG = "webdriver.edge.verboseLogging";
    public static final String SILENT_MODE = "webdriver.edge.silentOutput";
    public static final String WHITELISTED = "webdriver.edge.whitelistedIps";
    public static final String OPTIONS_KEY = "ms:edgeOptions";
    public static final String USE_CHROMIUM = "ms:edgeChromium";
    private static final String[] PROPERTY_NAMES = 
        { DRIVER_PATH, BINARY_PATH, LOGFILE_PATH, VERBOSE_LOG, SILENT_MODE, WHITELISTED };
    
    private static final String CAPABILITIES =
            "{\"browserName\":\"MicrosoftEdge\"}";
    
    private static final String BASELINE = 
            "{\"browserName\":\"MicrosoftEdge\",\"ms:edgeChromium\":true" +
             "\"nord:options\":{\"personality\":\"MicrosoftEdge\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.EdgePlugin\"}}";
    
    private static final String HEADLESS =
            "{\"browserName\":\"MicrosoftEdge\",\"ms:edgeChromium\":true," +
             "\"ms:edgeOptions\":{\"args\":[\"--headless\",\"--disable-gpu\"]}," +
             "\"nord:options\":{\"personality\":\"MicrosoftEdge.headless\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.EdgePlugin\"}}";

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
