package com.nordstrom.automation.selenium;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileLocatorUtils;
import org.apache.commons.configuration2.io.FileSystem;
import org.openqa.grid.internal.utils.configuration.GridHubConfiguration;
import org.openqa.grid.internal.utils.configuration.GridNodeConfiguration;
import org.openqa.selenium.Capabilities;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nordstrom.automation.settings.SettingsCore;

/**
 * This class contains declarations and methods related to Selenium WebDriver and Grid configuration.
 * 
 * @see SettingsCore
 */
public class SeleniumConfig extends SettingsCore<SeleniumConfig.SeleniumSettings> {
	
	private static final String SETTINGS_FILE = "settings.properties";
	private static final String CONFIG = "CONFIG";
	private static final String JSON_HEAD = "{ \"capabilities\": [";
	private static final String DEFAULT_CAPS = "{\"browserName\" : \"phantomjs\"}";
	private static final String JSON_TAIL = "] }";
	
	public static final String SELENIUM_LOGGER = "selenium.LOGGER";
	public static final String SELENIUM_LOGGER_LEVEL = "selenium.LOGGER.level";
	
	public enum SeleniumSettings implements SettingsCore.SettingsAPI {
		LOGGER("selenium.logger", null),
		LOGGER_LEVEL("selenium.logger.level", "WARNING"),
		HUB_CONFIG("selenium.hub.config", "hubConfig.json"),
		HUB_HOST("selenium.hub.host", null),
		HUB_PORT("selenuim.hub.port", null),
		NODE_CONFIG("selenium.node.config", "nodeConfig.json"),
		NODE_HOST("selenium.node.host", null),
		NODE_PORT("selenium.node.port", null),
		BROWSER_CAPS("selenium.browser.caps", DEFAULT_CAPS);
		
		private String propertyName;
		private String defaultValue;
		
		SeleniumSettings(String propertyName, String defaultValue) {
			this.propertyName = propertyName;
			this.defaultValue = defaultValue;
		}
		
		@Override
		public String key() {
			return propertyName;
		}

		@Override
		public String val() {
			return defaultValue;
		}
	}
	
	private GridNodeConfiguration nodeConfig;
	private String[] nodeArgs;
	private GridHubConfiguration hubConfig;
	private String[] hubArgs;
	private Capabilities browserCaps;
	
	public SeleniumConfig() throws ConfigurationException, IOException {
		super(SeleniumSettings.class);
	}

	/**
	 * Get the Selenium configuration object for the current context.
	 * 
	 * @return Selenium configuration object
	 */
	public static SeleniumConfig getConfig() {
		return getConfig(Reporter.getCurrentTestResult());
	}
	
	/**
	 * Get the Selenium configuration object for the specified context.
	 * 
	 * @param testResult configuration context (TestNG test result object)
	 * @return Selenium configuration object
	 */
	public static SeleniumConfig getConfig(ITestResult testResult) {
		if (testResult.getAttribute(CONFIG) == null) {
			synchronized (CONFIG) {
				if (testResult.getAttribute(CONFIG) == null) {
					try {
						testResult.setAttribute(CONFIG, new SeleniumConfig());
					} catch (ConfigurationException | IOException e) {
						throw new RuntimeException("Failed to instantiate settings", e);
					}
				}
			}
		}
		return (SeleniumConfig) testResult.getAttribute(CONFIG);
	}
	
	/**
	 * Get the Selenium Grid node configuration.
	 * 
	 * @return Selenium Grid node configuration
	 */
	public GridNodeConfiguration getNodeConfig() {
		if (nodeConfig == null) {
			String path = getConfigPath(getString(SeleniumSettings.NODE_CONFIG.key()));
			nodeConfig = resolveNodeSettings(GridNodeConfiguration.loadFromJSON(path));
		}
		return nodeConfig;
	}
	
	/**
	 * Get the arguments needed to launch a local Selenium Grid node.
	 * 
	 * @return array of node launch arguments
	 */
	public String[] getNodeArgs() {
		if (nodeArgs == null) {
			GridNodeConfiguration config = getNodeConfig();
			String configPath = getConfigPath(getString(SeleniumSettings.NODE_CONFIG.key()));
			nodeArgs = new String[] {"-role", "node", "-nodeConfig", configPath, "-host", 
					config.host, "-port", config.port.toString(), "-hub", config.hub};
		}
		return nodeArgs;
	}

	/**
	 * Resolve Selenium Grid node settings for host, port, and hub.
	 * 
	 * @param nodeConfig node configuration with unresolved settings
	 * @return node configuration with resolved settings
	 */
	private GridNodeConfiguration resolveNodeSettings(GridNodeConfiguration nodeConfig) {
		String nodeHost = getString(SeleniumSettings.NODE_HOST.key());
		if (nodeHost != null)  nodeConfig.host = nodeHost;
		if (nodeConfig.host == null) nodeConfig.host = getLocalHost();
		
		Integer nodePort = getInteger(SeleniumSettings.NODE_PORT.key(), null);
		if (nodePort != null) nodeConfig.port = nodePort;
		if (nodeConfig.port == null) nodeConfig.port = Integer.valueOf(5555);
		
		GridHubConfiguration hubConfig = getHubConfig();
		nodeConfig.hub = "http://" + hubConfig.host + ":" + hubConfig.port + "/grid/register/";
		
		return nodeConfig;
	}

	/**
	 * Get the Selenium Grid hub configuration.
	 * 
	 * @return Selenium Grid hub configuration
	 */
	public GridHubConfiguration getHubConfig() {
		if (hubConfig == null) {
			String path = getConfigPath(getString(SeleniumSettings.HUB_CONFIG.key()));
			hubConfig = resolveHubSettings(GridHubConfiguration.loadFromJSON(path));
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
			GridHubConfiguration config = getHubConfig();
			String configPath = getConfigPath(getString(SeleniumSettings.HUB_CONFIG.key()));
			hubArgs = new String[] {"-role", "hub", "-hubConfig", configPath, 
					"-host", config.host, "-port", config.port.toString()};
		}
		return hubArgs;
	}
	
	/**
	 * Resolve Selenium Grid hub settings for host and port.
	 * 
	 * @param hubConfig node configuration with unresolved settings
	 * @return hub configuration with resolved settings
	 */
	private GridHubConfiguration resolveHubSettings(GridHubConfiguration hubConfig) {
		String hubHost = getString(SeleniumSettings.HUB_HOST.key());
		if (hubHost != null)  hubConfig.host = hubHost;
		if (hubConfig.host == null) hubConfig.host = getLocalHost();
		
		Integer hubPort = getInteger(SeleniumSettings.HUB_PORT.key(), null);
		if (hubPort != null) hubConfig.port = hubPort;
		if (hubConfig.port == null) hubConfig.port = Integer.valueOf(4444);
		
		return hubConfig;
	}

	/**
	 * Get Internet protocol IP address for the machine we're running on.
	 * 
	 * @return IP address for the machine we're running on (a.k.a. - 'localhost')
	 */
	private static String getLocalHost() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
		}
		return "localhost";
	}

	/**
	 * Convert the configured browser specification from JSON to {@link Capabilities} object.
	 * 
	 * @return {@link Capabilities} object for the configured browser specification
	 */
	public Capabilities getBrowserCaps() {
		if (browserCaps == null) {
			JsonParser parser = new JsonParser();
			String jsonStr = getString(SeleniumSettings.BROWSER_CAPS.key());
			JsonObject jsonObj = parser.parse(JSON_HEAD + jsonStr + JSON_TAIL).getAsJsonObject();
			GridNodeConfiguration config = GridNodeConfiguration.loadFromJSON(jsonObj);
			browserCaps = config.capabilities.get(0);
		}
		return browserCaps;
	}

	/**
	 * Get the path to the specified configuration file.
	 * 
	 * @param path configuration file path (absolute, relative, or simple filename)
	 * @return resolved absolute path of specified file; 'null' if file not found
	 */
	private String getConfigPath(String path) {
		FileHandler handler = new FileHandler();
		handler.setPath(path);
		
		FileLocator locator = handler.getFileLocator();
		FileSystem fileSystem = FileLocatorUtils.DEFAULT_FILE_SYSTEM;
		FileLocationStrategy strategy = FileLocatorUtils.DEFAULT_LOCATION_STRATEGY;
		
		URL url = strategy.locate(fileSystem, locator);
		if (url != null) {
			try {
				URI uri = url.toURI();
				File file = new File(uri);
				return file.getAbsolutePath();
			} catch (URISyntaxException e) {
			}
		}
		return null;
	}
	
	@Override
	public String getSettingsPath() {
		return SETTINGS_FILE;
	}
}
