package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HtmlUnitCaps {
    
    private HtmlUnitCaps() {
        throw new AssertionError("HtmlUnitCaps is a static constants class that cannot be instantiated");
    }

    private static final String CAPABILITIES =
                    "{\"browserName\":\"htmlunit\", \"maxInstances\":5, \"seleniumProtocol\":\"WebDriver\", \"javascriptEnabled\":true}";
    
    public static final String HTMLUNIT = "{\"browserName\":\"htmlunit\"}";
    
    private static final Map<String, String> PERSONALITIES;
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put("htmlunit", HTMLUNIT);
        PERSONALITIES = Collections.unmodifiableMap(personalities);
    }
    
    public static String getCapabilities() {
        return CAPABILITIES;
    }

    public static Map<String, String> getPersonalities() {
        return PERSONALITIES;
    }

}
