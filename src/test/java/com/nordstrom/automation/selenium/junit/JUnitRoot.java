package com.nordstrom.automation.selenium.junit;

import static com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings.HUB_HOST;
import static com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings.TARGET_HOST;
import static com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings.TARGET_PORT;

import java.net.URI;

import org.junit.Before;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.GridUtility;

public class JUnitRoot extends JUnitBase {

	@Before
	public void beforeMethod() {
		SeleniumConfig config = SeleniumConfig.getConfig();
		setDriver(GridUtility.getDriver());
		URI hubUri = URI.create(config.getString(HUB_HOST.key()));
		System.setProperty(TARGET_HOST.key(), hubUri.getHost());
		System.setProperty(TARGET_PORT.key(), Integer.toString(hubUri.getPort()));
	}

}
