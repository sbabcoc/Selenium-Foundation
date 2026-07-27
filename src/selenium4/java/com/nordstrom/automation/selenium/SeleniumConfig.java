package com.nordstrom.automation.selenium;

import static org.openqa.selenium.json.Json.LIST_OF_MAPS_TYPE;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.json.Json;

import com.nordstrom.automation.settings.SettingsCore;

/**
 * This class declares settings and methods related to WebDriver and Grid configuration specific to the Selenium 3 API.
 * 
 * @see SettingsCore
 */
public class SeleniumConfig extends AbstractSeleniumConfig {
    
    private static final String DEFAULT_GRID_LAUNCHER = "org.openqa.selenium.grid.Bootstrap";
    private static final String DEFAULT_HUB_PORT = "4444";
    private static final String DEFAULT_HUB_CONFIG = "hubConfig-s4.json";
    private static final String DEFAULT_NODE_CONFIG = "nodeConfig-s4.json";
    
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
        return 4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isW3C() {
        return true;
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
        String capsList = (capabilities.startsWith("[")) ? capabilities : "[" + capabilities + "]";
        List<Map<String, Object>> capsMapList = new Json().toType(capsList, LIST_OF_MAPS_TYPE);
        return capsMapList.stream().map(MutableCapabilities::new).collect(Collectors.toList()).toArray(new Capabilities[0]);
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
