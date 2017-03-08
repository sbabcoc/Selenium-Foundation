package com.nordstrom.automation.selenium;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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

public class SeleniumConfig extends SettingsCore<SeleniumConfig.SeleniumSettings> {
	
	private static final String SETTINGS_FILE = "settings.properties";
	private static final String CONFIG = "CONFIG";
	private static final String JSON_HEAD = "{ \"capabilities\": [";
	private static final String DEFAULT_CAPS = "{\"browserName\" : \"chrome\"}";
	private static final String JSON_TAIL = "] }";
	
	private GridNodeConfiguration nodeConfig;
	private GridHubConfiguration hubConfig;
	private Capabilities browserCaps;
	
	public SeleniumConfig() throws ConfigurationException, IOException {
		super(SeleniumSettings.class);
	}

	public static SeleniumConfig getConfig() {
		return getConfig(Reporter.getCurrentTestResult());
	}
	
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
	
	public GridNodeConfiguration getNodeConfig() {
		if (nodeConfig == null) {
			String path = getConfigPath(getString(SeleniumSettings.NODE_CONFIG.key()));
			nodeConfig = GridNodeConfiguration.loadFromJSON(path);
		}
		return nodeConfig;
	}
	
	public GridHubConfiguration getHubConfig() {
		if (hubConfig == null) {
			String path = getConfigPath(getString(SeleniumSettings.HUB_CONFIG.key()));
			hubConfig = GridHubConfiguration.loadFromJSON(path);
		}
		return hubConfig;
	}
	
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
	
	public enum SeleniumSettings implements SettingsCore.SettingsAPI {
		HUB_CONFIG("selenium.hub.config", "hubConfig.json"),
		NODE_CONFIG("selenium.node.config", "nodeConfig.json"),
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

}
