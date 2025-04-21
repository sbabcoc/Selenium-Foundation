package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

/**
 * This class is the plug-in for the <b>UiAutomator2</b> engine of <b>Appium</b>
 */
public class UiAutomator2Plugin extends AbstractAppiumPlugin {

    /** driver name */
    public static final String DRIVER_NAME = "UiAutomator2";
    
    /**
     * Constructor for <b>UiAutomator2Plugin</b> objects.
     */
    public UiAutomator2Plugin() {
        super(DRIVER_NAME);
    }

    private static final String CAPABILITIES =
            "{\"appium:automationName\":\"UiAutomator2\",\"platformName\":\"Android\"," +
             "\"browserName\":\"Chrome\",\"appium:deviceName\":\"Android Emulator\"}";
    
    private static final String BASELINE =
            "{\"appium:automationName\":\"UiAutomator2\",\"platformName\":\"Android\"," +
             "\"nord:options\":{\"personality\":\"UiAutomator2\"," +
                                "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.UiAutomator2Plugin\"}}";
    
    private static final Map<String, String> PERSONALITIES;
    
    private static final String DRIVER_CLASS_NAME = "io.appium.java_client.android.AndroidDriver";
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(DRIVER_NAME, BASELINE);
        PERSONALITIES = Collections.unmodifiableMap(personalities);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getCapabilities(SeleniumConfig config) {
        return addNordOptions(config, CAPABILITIES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getPersonalities() {
        return PERSONALITIES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDriverClassName() {
        return DRIVER_CLASS_NAME;
    }

}
