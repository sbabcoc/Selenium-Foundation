package com.nordstrom.automation.selenium.plugins;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class ChromeCaps {
    
    private ChromeCaps() {
        throw new AssertionError("ChromeCaps is a static constants class that cannot be instantiated");
    }

    public static final String DRIVER_NAME = "chrome";
    public static final String DRIVER_PATH = "webdriver.chrome.driver";
    public static final String BINARY_PATH = "selenium.chrome.binary";
    public static final String OPTIONS_KEY = "goog:chromeOptions";
    public static final String BINARY_KEY = "binary";
    private static final String[] PROPERTY_NAMES = { DRIVER_PATH };
    
    private static final String CAPABILITIES =
            "{\"browserName\":\"chrome\",\"maxInstances\":5,\"seleniumProtocol\":\"WebDriver\"}";

    private static final String BASELINE =
                    "{\"browserName\":\"chrome\"," +
                     "\"goog:chromeOptions\":{" +
                         "\"args\":[\"--disable-infobars\"]," +
                         "\"prefs\":{\"credentials_enable_service\":false}}" +
                    "}";
    
    private static final String HEADLESS =
                    "{\"browserName\":\"chrome\"," +
                     "\"goog:chromeOptions\":{" +
                         "\"args\":[\"--disable-infobars\",\"--headless\",\"--disable-gpu\"]," +
                         "\"prefs\":{\"credentials_enable_service\":false}}," +
                     "\"personality\":\"chrome.headless\"" +
                    "}";
    
    private static final Map<String, String> PERSONALITIES;
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(DRIVER_NAME, BASELINE);
        personalities.put(DRIVER_NAME + ".headless", HEADLESS);
        PERSONALITIES = Collections.unmodifiableMap(personalities);
    }
    
    @SuppressWarnings("unchecked")
    public static String getCapabilities(SeleniumConfig config) {
        // if configuration specifies binary path
        if (config.getString(BINARY_PATH) != null) {
            // assemble mutable capabilities object
            DesiredCapabilities caps = new DesiredCapabilities()
                    .merge(config.getCapabilitiesForJson(CAPABILITIES)[0]);
            
            // get currently specified driver options
            Map<String, Object> options = (Map<String, Object>) caps.getCapability(OPTIONS_KEY);
            // if none are specified, start fresh
            if (options == null) options = new HashMap<>();
            // store specified driver binary path
            options.put(BINARY_KEY, new File(config.getString(BINARY_PATH)).getPath());
            // store revised driver options
            caps.setCapability(OPTIONS_KEY, options);
            
            return config.toJson(caps);
        } else {
            return CAPABILITIES;
        }
    }

    public static Map<String, String> getPersonalities() {
        return PERSONALITIES;
    }
    
    public static String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

}
