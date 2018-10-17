package com.nordstrom.automation.selenium;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.openqa.grid.common.GridRole;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.selenium.Capabilities;

import com.nordstrom.automation.settings.SettingsCore;

/**
 * This class declares settings and methods related to WebDriver and Grid configuration specific to the Selenium 2 API.
 * 
 * @see SettingsCore
 */
@SuppressWarnings({"squid:S1200", "squid:S2972", "squid:MaximumInheritanceDepth"})
public class SeleniumConfig extends AbstractSeleniumConfig {
    
    private static final String HOST = "host";
    private static final String JSON_HEAD = "{ \"capabilities\": [";
    private static final String JSON_TAIL = "], \"configuration\": {} }";
    
    static {
        try {
            SELENIUM_CONFIG = new SeleniumConfig();
        } catch (ConfigurationException | IOException e) {
            throw new RuntimeException("Failed to instantiate settings", e); //NOSONAR
        }
    }
    
    private RegistrationRequest nodeConfig;
    private GridHubConfiguration hubConfig;
    
    public SeleniumConfig() throws ConfigurationException, IOException {
        super();
    }

    /**
     * Get the Selenium configuration object.
     * 
     * @return Selenium configuration object
     */
    public static SeleniumConfig getConfig() {
        return (SeleniumConfig) SELENIUM_CONFIG;
    }
    
    /**
     * Get the Selenium Grid node configuration.
     * 
     * @return Selenium Grid node configuration
     */
    public RegistrationRequest getNodeConfig() {
        if (nodeConfig == null) {
            nodeConfig = new RegistrationRequest();
            nodeConfig.loadFromJSON(getNodeConfigPath());
            nodeConfig = resolveNodeSettings(nodeConfig);
            
            // hack for RegistrationRequest bug
            nodeConfig.setRole(GridRole.NODE);
        }
        return nodeConfig;
    }
    
    /**
     * Get the arguments needed to launch a local Selenium Grid node.
     * 
     * @return array of node launch arguments
     */
    @Override
    public String[] getNodeArgs() {
        if (nodeArgs == null) {
            String configPath = getNodeConfigPath();
            Map<String, Object> config = getNodeConfig().getConfiguration();
            nodeArgs = new String[] {"-role", "node", "-nodeConfig", configPath, "-host", (String) config.get(HOST),
                    "-port", config.get("port").toString(), "-hub", (String) config.get("hub")};
        }
        return Arrays.copyOf(nodeArgs, nodeArgs.length);
    }

    /**
     * Resolve Selenium Grid node settings for host, port, and hub.
     * 
     * @param nodeConfig node configuration with unresolved settings
     * @return node configuration with resolved settings
     */
    private RegistrationRequest resolveNodeSettings(final RegistrationRequest nodeConfig) {
        Map<String, Object> config = nodeConfig.getConfiguration();
        
        // get configured Grid node host
        String nodeHost = getString(SeleniumSettings.NODE_HOST.key());
        // is host specified
        if (nodeHost != null) {
            // store specified host
            config.put(HOST, nodeHost);
        // otherwise
        } else {
            // use 'localhost' if host is unspecified
            config.computeIfAbsent(HOST, k -> getLocalHost());
        }
        
        // set configured (or default) Grid node port
        config.put("port", getInteger(SeleniumSettings.NODE_PORT.key(), null));
        // set Grid hub registration URL
        config.put("hub", "http://" + getHubConfig().getHost() + ":" + getHubConfig().getPort() + "/grid/register/");
        
        return nodeConfig;
    }
    
    /**
     * Get the Selenium Grid hub configuration.
     * 
     * @return Selenium Grid hub configuration
     */
    public GridHubConfiguration getHubConfig() {
        if (hubConfig == null) {
            hubConfig = new GridHubConfiguration();
            hubConfig.loadFromJSON(getHubConfigPath());
            hubConfig = resolveHubSettings(hubConfig);
        }
        return hubConfig;
    }
    
    /**
     * Get the arguments needed to launch a local Selenium Grid hub.
     * 
     * @return array of hub launch arguments
     */
    public String[] getHubArgs() {
        if (hubArgs == null) {
            String configPath = getHubConfigPath();
            GridHubConfiguration config = getHubConfig();
            hubArgs = new String[] {"-role", "hub", "-hubConfig", configPath, 
                    "-host", config.getHost(), "-port", Integer.toString(config.getPort())};
        }
        return Arrays.copyOf(hubArgs, hubArgs.length);
    }
    
    /**
     * Resolve Selenium Grid hub settings for host and port.
     * 
     * @param hubConfig node configuration with unresolved settings
     * @return hub configuration with resolved settings
     */
    private GridHubConfiguration resolveHubSettings(final GridHubConfiguration hubConfig) {
        String hubHost = getString(SeleniumSettings.HUB_HOST.key());
        if (hubHost != null) {
            hubConfig.setHost(hubHost);
        }
        if (hubConfig.getHost() == null) {
            hubConfig.setHost(getLocalHost());
        }
        
        Integer hubPort = getInteger(SeleniumSettings.HUB_PORT.key(), null);
        if (hubPort != null) {
            hubConfig.setPort(hubPort.intValue());
        }
        
        return hubConfig;
    }

    /**
     * Convert the configured browser specification from JSON to {@link Capabilities} object.
     * 
     * @return {@link Capabilities} object for the configured browser specification
     */
    public Capabilities getBrowserCaps() {
        if (browserCaps == null) {
            String jsonStr = getJsonForName(getString(SeleniumSettings.BROWSER_NAME.key()));
            
            if (jsonStr == null) {
                jsonStr = getString(SeleniumSettings.BROWSER_CAPS.key());
            }
            
            RegistrationRequest config = RegistrationRequest.getNewInstance(JSON_HEAD + jsonStr + JSON_TAIL);
            browserCaps = config.getCapabilities().get(0);
        }
        return browserCaps;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> getDefaults() {
        Map<String, String> defaults = super.getDefaults();
        defaults.put(SeleniumSettings.HUB_PORT.key(), "4444");
        defaults.put(SeleniumSettings.NODE_PORT.key(), "5555");
        defaults.put(SeleniumSettings.NODE_CONFIG.key(), "nodeConfig-s2.json");
        return defaults;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getHubHost() {
        return getConfig().getHubConfig().getHost();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getHubPort() {
        return getConfig().getHubConfig().getPort();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getNodeHost() {
        return (String) getConfig().getNodeConfig().getConfiguration().get("host");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getNodePort() {
        return (Integer) getConfig().getNodeConfig().getConfiguration().get("port");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getNodeShutdownRequest() {
        return "/selenium-server/driver/?cmd=shutDownSeleniumServer";
    }
}
