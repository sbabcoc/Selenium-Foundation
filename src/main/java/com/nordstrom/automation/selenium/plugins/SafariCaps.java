package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SafariCaps {
    
    private SafariCaps() {
        throw new AssertionError("SafariCaps is a static constants class that cannot be instantiated");
    }

    private static final String CAPABILITIES =
                    "{\"browserName\":\"safari\",\"maxInstances\":5,\"seleniumProtocol\":\"WebDriver\"}";
    
    public static final String BROWSER_NAME = "safari";
    public static final String OPTIONS_KEY = "safari.options";
    public static final String BASELINE = "{\"browserName\":\"safari\"}";
    
    private static final String[] PROPERTY_NAMES = {
                    "webdriver.safari.driver",
                    "webdriver.safari.noinstall"};
    
    private static final Map<String, String> PERSONALITIES;
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(BROWSER_NAME, BASELINE);
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
