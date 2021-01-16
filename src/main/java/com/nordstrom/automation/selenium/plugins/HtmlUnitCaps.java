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
                    "{\"browserName\":\"htmlunit\"," +
                     "\"browserVersion\":\"chrome\"," +
                     "\"maxInstances\":5," +
                     "\"seleniumProtocol\":\"WebDriver\"}";
    
    private static final String BASELINE = "{\"browserName\":\"htmlunit\",\"browserVersion\":\"chrome\"}";
    
    private static final String CHROME = 
                    "{\"browserName\":\"htmlunit\"," +
                     "\"browserVersion\":\"chrome\"," +
                     "\"personality\":\"htmlunit.chrome\"}";
    
    private static final String FIREFOX = 
                    "{\"browserName\":\"htmlunit\"," +
                     "\"browserVersion\":\"firefox\"," +
                     "\"personality\":\"htmlunit.firefox\"}";
    
    private static final String INT_EXP = 
                    "{\"browserName\":\"htmlunit\"," +
                     "\"browserVersion\":\"ie\"," +
                     "\"personality\":\"htmlunit.ie\"}";
    
    private static final Map<String, String> PERSONALITIES;
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(DRIVER_NAME, BASELINE);
        personalities.put(DRIVER_NAME + ".chrome", CHROME);
        personalities.put(DRIVER_NAME + ".firefox", FIREFOX);
        personalities.put(DRIVER_NAME + ".ie", INT_EXP);
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
