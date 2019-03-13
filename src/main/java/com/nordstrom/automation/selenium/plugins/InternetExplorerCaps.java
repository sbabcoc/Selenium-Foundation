package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InternetExplorerCaps {
    
    private InternetExplorerCaps() {
        throw new AssertionError("InternetExplorerCaps is a static constants class that cannot be instantiated");
    }

    private static final String CAPABILITIES =
                    "{\"browserName\":\"internet explorer\", \"maxInstances\":1, \"seleniumProtocol\":\"WebDriver\"}";
    
    public static final String BROWSER_NAME = "internet explorer";
    public static final String OPTIONS_KEY = "se:ieOptions";
    public static final String BASELINE = "{\"browserName\":\"internet explorer\"}";
    
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
