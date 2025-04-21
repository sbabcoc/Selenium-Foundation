package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

/**
 * This class is the plug-in for the <b>Espresso</b> engine of <b>Appium</b>
 */
public class EspressoPlugin extends AbstractAppiumPlugin {

    /** driver name */
    public static final String DRIVER_NAME = "Espresso";
    
    /**
     * Constructor for <b>EspressoPlugin</b> objects.
     */
    public EspressoPlugin() {
        super(DRIVER_NAME);
    }

    private static final String CAPABILITIES =
            "{\"appium:automationName\":\"Espresso\",\"platformName\":\"Android\"}";
    
    private static final String BASELINE =
            "{\"appium:automationName\":\"Espresso\",\"platformName\":\"Android\"," +
             "\"nord:options\":{\"personality\":\"Espresso\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.EspressoPlugin\"}}";
    
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
