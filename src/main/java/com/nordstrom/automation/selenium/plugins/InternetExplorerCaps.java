package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InternetExplorerCaps {
    
    private InternetExplorerCaps() {
        throw new AssertionError("InternetExplorerCaps is a static constants class that cannot be instantiated");
    }

    public static final String DRIVER_NAME = "internet explorer";
    public static final String OPTIONS_KEY = "se:ieOptions";
    private static final String[] PROPERTY_NAMES = { "webdriver.ie.driver" };
    
    private static final String CAPABILITIES =
                    "{\"browserName\":\"internet explorer\",\"maxInstances\":1,\"seleniumProtocol\":\"WebDriver\"}";
    
    private static final String BASELINE = "{\"browserName\":\"internet explorer\"}";
    
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
