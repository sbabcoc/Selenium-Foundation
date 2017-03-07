package com.nordstrom.automation.selenium;

import java.io.IOException;

import org.apache.commons.configuration2.ex.ConfigurationException;

import com.nordstrom.automation.settings.SettingsCore;

public class SeleniumConfig extends SettingsCore<SeleniumConfig.SeleniumSettings> {
	
	public SeleniumConfig() throws ConfigurationException, IOException {
		super(SeleniumSettings.class);
	}

	public enum SeleniumSettings implements SettingsCore.SettingsAPI {
		GRID_HOST("selenium.grid.host", "localhost"),
		GRID_SCHEME("selenium.grid.scheme", "http"),
		GRID_PORT("selenium.grid.port", "4444");
		
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
