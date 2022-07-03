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
            "{\"appium:automationName\":\"Windows\"," +
             "\"platformName\":\"Windows\"," +
             "\"maxInstances\":5}";
    
    private static final String BASELINE =
            "{\"appium:automationName\":\"Windows\"," + 
             "\"platformName\":\"Windows\"," +
             "\"personality\":\"Windows\"," +
             "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.WindowsPlugin\"" +
            "}";
    
    private static final Map<String, String> PERSONALITIES;
    
    private static final String DRIVER_CLASS_NAME = "io.appium.java_client.windows.WindowsDriver";
    
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

    @Override
    public String getDriverClassName() {
        return DRIVER_CLASS_NAME;
    }

}
