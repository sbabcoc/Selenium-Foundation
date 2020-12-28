package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class WindowsPlugin extends AbstractAppiumPlugin {

    public static final String DRIVER_NAME = "Windows";
    
    public WindowsPlugin() {
        super(DRIVER_NAME);
    }

    private static final String CAPABILITIES =
            "{\"automationName\":\"Windows\"," +
             "\"platformName\":\"Windows\"," +
             "\"maxInstances\":5}";
    
    private static final String BASELINE =
            "{\"automationName\":\"Windows\"," + 
             "\"platformName\":\"Windows\"}";
    
    private static final Map<String, String> PERSONALITIES;
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(DRIVER_NAME, BASELINE);
        PERSONALITIES = Collections.unmodifiableMap(personalities);
    }
    
    @Override
    public String getCapabilities(SeleniumConfig config) {
        return CAPABILITIES;
    }

    @Override
    public Map<String, String> getPersonalities() {
        return PERSONALITIES;
    }

}
