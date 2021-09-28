package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FirefoxCaps {
    
    private FirefoxCaps() {
        throw new AssertionError("FirefoxCaps is a static constants class that cannot be instantiated");
    }

    public static final String DRIVER_NAME = "firefox";
    public static final String DRIVER_PATH = "webdriver.gecko.driver";
    public static final String BINARY_PATH = "webdriver.firefox.bin";
    public static final String OPTIONS_KEY = "moz:firefoxOptions";
    private static final String[] PROPERTY_NAMES = { DRIVER_PATH, BINARY_PATH };
    
    private static final String CAPABILITIES =
                    "{\"browserName\":\"firefox\",\"maxInstances\":5,\"seleniumProtocol\":\"WebDriver\"}";
    
    private static final String BASELINE =
                    "{\"browserName\":\"firefox\"," +
                     "\"marionette\":true}";
    
    private static final String HEADLESS =
                    "{\"browserName\":\"firefox\"," +
                     "\"marionette\":true," +
                     "\"moz:firefoxOptions\":{\"args\":[\"-headless\"]}," +
                     "\"personality\":\"firefox.headless\"" +
                    "}";
    
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

    public static String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

}
