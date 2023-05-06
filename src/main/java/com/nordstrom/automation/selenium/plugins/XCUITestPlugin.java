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
            "{\"appium:automationName\":\"XCUITest\",\"platformName\":\"iOS\",\"browserName\":\"Safari\"," +
             "\"appium:deviceName\":\"iPhone Simulator\"}";
    
    private static final String BASELINE =
            "{\"appium:automationName\":\"XCUITest\",\"platformName\":\"iOS\"," +
             "\"nord:options\":{\"personality\":\"XCUITest\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.XCUITestPlugin\"}}";
    
    private static final Map<String, String> PERSONALITIES;
    
    private static final String DRIVER_CLASS_NAME = "io.appium.java_client.ios.IOSDriver";
    
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
