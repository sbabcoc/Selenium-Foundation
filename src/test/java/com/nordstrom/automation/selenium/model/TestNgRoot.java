package com.nordstrom.automation.selenium.model;

import static com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings.HUB_HOST;
import static com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings.TARGET_HOST;
import static com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings.TARGET_PORT;

import java.net.URI;

import org.testng.annotations.BeforeMethod;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.support.TestNgBase;

public class TestNgRoot extends TestNgBase {
	
	@BeforeMethod
	public void beforeMethod() {
		SeleniumConfig config = SeleniumConfig.getConfig();
		setDriver(GridUtility.getDriver());
		URI hubUri = URI.create(config.getString(HUB_HOST.key()));
		System.setProperty(TARGET_HOST.key(), hubUri.getHost());
		System.setProperty(TARGET_PORT.key(), Integer.toString(hubUri.getPort()));
	}

}
