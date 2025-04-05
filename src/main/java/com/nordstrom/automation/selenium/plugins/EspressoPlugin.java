package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class EspressoPlugin extends AbstractAppiumPlugin {

    public static final String DRIVER_NAME = "Espresso";
    
    public EspressoPlugin() {
        super(DRIVER_NAME);
    }

    private static final String CAPABILITIES =
            "{\"appium:automationName\":\"Espresso\",\"platformName\":\"Android\"}," +
            "{\"appium:automationName\":\"Espresso\",\"platformName\":\"Android\",\"browserName\":\"chrome\"}";
    
    private static final String BASELINE =
            "{\"appium:automationName\":\"Espresso\",\"platformName\":\"Android\"," +
             "\"nord:options\":{\"personality\":\"Espresso\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.EspressoPlugin\"}}";
    
    private static final String CHROME =
            "{\"appium:automationName\":\"Espresso\",\"platformName\":\"Android\",\"browserName\":\"chrome\"," +
                    "\"nord:options\":{\"personality\":\"Espresso.chrome\"," +
                                      "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.EspressoPlugin\"}}";
    
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
