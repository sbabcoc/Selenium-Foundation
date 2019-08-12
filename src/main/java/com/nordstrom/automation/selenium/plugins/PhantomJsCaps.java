package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PhantomJsCaps {
    
    private PhantomJsCaps() {
        throw new AssertionError("PhantomJsCaps is a static constants class that cannot be instantiated");
    }

    private static final String CAPABILITIES =
                    "{\"browserName\": \"phantomjs\", \"maxInstances\": 5, \"seleniumProtocol\": \"WebDriver\"}";
    
    public static final String BROWSER_NAME = "phantomjs";
    public static final String PHANTOMJS = "{\"browserName\":\"phantomjs\"}";
    
    private static final String[] PROPERTY_NAMES = {
                    "phantomjs.binary.path",
                    "phantomjs.ghostdriver.path",
                    "phantomjs.logfile.path"};
    
    private static final Map<String, String> PERSONALITIES;
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(BROWSER_NAME, PHANTOMJS);
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
