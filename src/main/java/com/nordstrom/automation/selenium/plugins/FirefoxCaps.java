package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FirefoxCaps {
    
    private FirefoxCaps() {
        throw new AssertionError("FirefoxCaps is a static constants class that cannot be instantiated");
    }

    private static final String CAPABILITIES =
                    "{\"browserName\":\"firefox\", \"maxInstances\":5, \"seleniumProtocol\":\"WebDriver\"}";
    
    public static final String BROWSER_NAME = "firefox";
    public static final String OPTIONS_KEY = "moz:firefoxOptions";
    public static final String BASELINE = "{\"browserName\":\"firefox\"}";
    
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

}
