package com.nordstrom.automation.selenium.plugins;

import java.util.Map;
import com.nordstrom.automation.selenium.SeleniumConfig;

/**
 * This class is the plug-in for <b>OperaDriver</b>.
 */
public class OperaPlugin extends ChromePlugin {
    
    /**
     * Constructor for <b>OperaPlugin</b> objects.
     */
    public OperaPlugin() {
        super(OperaCaps.DRIVER_NAME);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getCapabilities(SeleniumConfig config) {
        return OperaCaps.getCapabilities();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getPersonalities() {
        return OperaCaps.getPersonalities();
    }
}
