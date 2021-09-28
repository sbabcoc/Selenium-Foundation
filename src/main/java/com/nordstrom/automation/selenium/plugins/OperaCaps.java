package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OperaCaps {
    
    private OperaCaps() {
        throw new AssertionError("OperaCaps is a static constants class that cannot be instantiated");
    }

    public static final String DRIVER_NAME = "opera";
    public static final String DRIVER_PATH = "webdriver.opera.driver";
    public static final String BINARY_PATH = "webdriver.opera.bin";
    public static final String OPTIONS_KEY = "operaOptions";
    private static final String[] PROPERTY_NAMES = { DRIVER_PATH, BINARY_PATH };
    
    private static final String CAPABILITIES = "{\"browserName\":\"opera\",\"maxInstances\":5,\"seleniumProtocol\":\"WebDriver\"}";
    
    private static final String BASELINE = "{\"browserName\":\"opera\"}";
    
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

    public static String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

}
