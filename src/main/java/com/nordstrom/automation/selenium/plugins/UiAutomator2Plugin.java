package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class UiAutomator2Plugin extends AbstractAppiumPlugin {

    public static final String DRIVER_NAME = "UiAutomator2";
    
    public UiAutomator2Plugin() {
        super(DRIVER_NAME);
    }

    private static final String CAPABILITIES =
            "{\"appium:automationName\":\"UiAutomator2\",\"platformName\":\"Android\"}," +
            "{\"appium:automationName\":\"UiAutomator2\",\"platformName\":\"Android\",\"browserName\":\"chrome\"}";
    
    private static final String BASELINE =
            "{\"appium:automationName\":\"UiAutomator2\",\"platformName\":\"Android\"," +
             "\"nord:options\":{\"personality\":\"UiAutomator2\"," +
                                "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.UiAutomator2Plugin\"}}";
    
    private static final String CHROME =
            "{\"appium:automationName\":\"UiAutomator2\",\"platformName\":\"Android\",\"browserName\":\"chrome\"," +
                    "\"nord:options\":{\"personality\":\"UiAutomator2.chrome\"," +
                                      "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.UiAutomator2Plugin\"}}";
    
    private static final Map<String, String> PERSONALITIES;
    
    private static final String DRIVER_CLASS_NAME = "io.appium.java_client.android.AndroidDriver";
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(DRIVER_NAME, BASELINE);
        personalities.put(DRIVER_NAME + ".chrome", CHROME);
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
