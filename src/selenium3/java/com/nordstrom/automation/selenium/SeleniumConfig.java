package com.nordstrom.automation.selenium;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.openqa.grid.internal.utils.configuration.GridNodeConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.JsonInput;

import com.nordstrom.automation.settings.SettingsCore;

/**
 * This class declares settings and methods related to WebDriver and Grid configuration specific to the Selenium 3 API.
 * 
 * @see SettingsCore
 */
public class SeleniumConfig extends AbstractSeleniumConfig {
    
    private static final String JSON_HEAD = "{ \"capabilities\": [";
    private static final String JSON_TAIL = "] }";
    private static final String DEFAULT_GRID_LAUNCHER = "org.openqa.grid.selenium.GridLauncherV3";
    private static final String DEFAULT_HUB_PORT = "4454";
    private static final String DEFAULT_HUB_CONFIG = "hubConfig-s3.json";
    private static final String DEFAULT_NODE_CONFIG = "nodeConfig-s3.json";
    
    static {
        try {
            seleniumConfig = new SeleniumConfig();
        } catch (ConfigurationException | IOException e) {
            throw new RuntimeException("Failed to instantiate settings", e);
        }
    }
    
    /**
     * Instantiate a <b>Selenium Foundation</b> configuration object.
     * 
     * @throws ConfigurationException If a failure is encountered while initializing this configuration object.
     * @throws IOException If a failure is encountered while reading from a configuration input stream.
     */
    public SeleniumConfig() throws ConfigurationException, IOException {
        super();
    }

    /**
     * Get the Selenium configuration object.
     * 
     * @return Selenium configuration object
     */
    public static SeleniumConfig getConfig() {
        return seleniumConfig;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getVersion() {
        return 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isW3C() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> getDefaults() {
        Map<String, String> defaults = new HashMap<>(super.getDefaults());
        defaults.put(SeleniumSettings.GRID_LAUNCHER.key(), DEFAULT_GRID_LAUNCHER);
        defaults.put(SeleniumSettings.HUB_PORT.key(), DEFAULT_HUB_PORT);
        defaults.put(SeleniumSettings.HUB_CONFIG.key(), DEFAULT_HUB_CONFIG);
        defaults.put(SeleniumSettings.NODE_CONFIG.key(), DEFAULT_NODE_CONFIG);
        defaults.put(SeleniumSettings.GRID_PORT_ALLOCATOR.key(),
                "com.nordstrom.automation.selenium.sidecar.DefaultGridPortAllocationStrategy");
        defaults.put(SeleniumSettings.SIDECAR_AUTH_STRATEGY.key(),
                "com.nordstrom.automation.selenium.sidecar.DefaultSidecarAuthStrategy");
        return defaults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Capabilities[] getCapabilitiesForJson(String capabilities) {
        JsonInput input = new Json().newInput(new StringReader(JSON_HEAD + capabilities + JSON_TAIL));
        return GridNodeConfiguration.loadFromJSON(input).capabilities.toArray(new Capabilities[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Capabilities mergeCapabilities(Capabilities target, Capabilities change) {
        return target.merge(change);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toJson(Object obj) {
        return new Json().toJson(obj);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T fromJson(String json, java.lang.reflect.Type typeOfT) {
        return new Json().toType(json, typeOfT);
    }
}
