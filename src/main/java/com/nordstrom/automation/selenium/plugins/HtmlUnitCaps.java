package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HtmlUnitCaps {
    
    private HtmlUnitCaps() {
        throw new AssertionError("HtmlUnitCaps is a static constants class that cannot be instantiated");
    }

    public static final String DRIVER_NAME = "htmlunit";
    private static final String[] PROPERTY_NAMES = {  };
    
    private static final String CAPABILITIES =
            "{\"browserName\":\"htmlunit\"}";
    
    private static final String BASELINE = 
             "{\"browserName\":\"htmlunit\"," +
              "\"nord:options\":{\"personality\":\"htmlunit\"," +
                                "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.HtmlUnitPlugin\"}}";
    
    private static final String CHROME = 
            "{\"browserName\":\"htmlunit\",\"browserVersion\":\"chrome\"," +
             "\"nord:options\":{\"personality\":\"htmlunit.chrome\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.HtmlUnitPlugin\"}}";
    
    private static final String FIREFOX = 
            "{\"browserName\":\"htmlunit\",\"browserVersion\":\"firefox\"," +
             "\"nord:options\":{\"personality\":\"htmlunit.firefox\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.HtmlUnitPlugin\"}}";
    
    private static final String INT_EXP = 
            "{\"browserName\":\"htmlunit\",\"browserVersion\":\"ie\"," +
             "\"nord:options\":{\"personality\":\"htmlunit.ie\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.HtmlUnitPlugin\"}}";
    
    private static final String NO_JS = 
            "{\"browserName\":\"htmlunit\",\"javascriptEnabled\":false," +
             "\"nord:options\":{\"personality\":\"htmlunit.nojs\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.HtmlUnitPlugin\"}}";
   
    private static final Map<String, String> PERSONALITIES;
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(DRIVER_NAME, BASELINE);
        personalities.put(DRIVER_NAME + ".chrome", CHROME);
        personalities.put(DRIVER_NAME + ".firefox", FIREFOX);
        personalities.put(DRIVER_NAME + ".ie", INT_EXP);
        personalities.put(DRIVER_NAME + ".nojs", NO_JS);
        PERSONALITIES = Collections.unmodifiableMap(personalities);
    }
    
    public static String getCapabilities() {
        return CAPABILITIES;
    }

    public static Map<String, String> getPersonalities() {
        return PERSONALITIES;
    }

    public static String[] getPropertyNames(String capabilities) {
        return PROPERTY_NAMES;
    }

}
