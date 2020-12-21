package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class XCUITestPlugin extends AbstractAppiumPlugin {

    public static final String DRIVER_NAME = "XCUITest";
    
    public XCUITestPlugin() {
        super(DRIVER_NAME);
    }

    private static final String CAPABILITIES =
            "{\"automationName\":\"XCUITest\"," +
             "\"platformName\":\"iOS\"," +
             "\"browserName\":\"Safari\"," +
             "\"maxInstances\":1," +
             "\"deviceName\":\"iPhone Simulator\"}";
    
    private static final String BASELINE =
            "{\"automationName\":\"XCUITest\"," + 
             "\"platformName\":\"iOS\"}";
    
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
