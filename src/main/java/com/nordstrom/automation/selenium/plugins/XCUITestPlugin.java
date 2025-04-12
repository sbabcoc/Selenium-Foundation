package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

/**
 * This class is the plug-in for the <b>XCUITest</b> engine of <b>Appium</b>
 */
public class XCUITestPlugin extends AbstractAppiumPlugin {

    /** driver name */
    public static final String DRIVER_NAME = "XCUITest";
    
    /**
     * Constructor for <b>XCUITestPlugin</b> objects.
     */
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
