package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class Mac2Plugin extends AbstractAppiumPlugin {

    public static final String DRIVER_NAME = "Mac2";
    
    public Mac2Plugin() {
        super(DRIVER_NAME);
    }

    private static final String CAPABILITIES =
            "{\"appium:automationName\":\"Mac2\",\"platformName\":\"Mac\"}";
    
    private static final String BASELINE =
            "{\"appium:automationName\":\"Mac2\",\"platformName\":\"Mac\"," +
             "\"nord:options\":{\"personality\":\"Mac2\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.Mac2Plugin\"}}";
    
    private static final Map<String, String> PERSONALITIES;
    
    private static final String DRIVER_CLASS_NAME = "io.appium.java_client.mac.Mac2Driver";
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(DRIVER_NAME, BASELINE);
        PERSONALITIES = Collections.unmodifiableMap(personalities);
    }
    
    @Override
    public String getCapabilities(SeleniumConfig config) {
        return addNordOptions(config, CAPABILITIES);
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
