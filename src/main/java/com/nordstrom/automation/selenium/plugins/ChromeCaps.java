package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChromeCaps {
    
    private ChromeCaps() {
        throw new AssertionError("ChromeCaps is a static constants class that cannot be instantiated");
    }

    private static final String CAPABILITIES =
                    "{\"browserName\":\"chrome\",\"maxInstances\":5,\"seleniumProtocol\":\"WebDriver\"}";
    
    public static final String BROWSER_NAME = "chrome";
    public static final String OPTIONS_KEY = "goog:chromeOptions";
    
    public static final String BASELINE =
                    "{\"browserName\":\"chrome\"," +
                     "\"goog:chromeOptions\":{" +
                         "\"args\":[\"--disable-infobars\"]," +
                         "\"prefs\":{\"credentials_enable_service\":false}" +
                    "}}";
    
    public static final String HEADLESS =
                    "{\"browserName\":\"chrome\"," +
                     "\"goog:chromeOptions\":{" +
                         "\"args\":[\"--disable-infobars\",\"--headless\",\"--disable-gpu\"]," +
                         "\"prefs\":{\"credentials_enable_service\":false}" +
                    "}}";
    
    private static final String[] PROPERTY_NAMES = { "webdriver.chrome.driver" };
    
    private static final Map<String, String> PERSONALITIES;
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(BROWSER_NAME, BASELINE);
        personalities.put("chrome.headless", HEADLESS);
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
