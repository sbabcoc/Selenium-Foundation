package com.nordstrom.automation.selenium.core;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.annotations.NoDriver;

public class GridUtilityTest {
	
	@Test
	@NoDriver
	public void testIsActive() throws UnknownHostException, MalformedURLException {
		SeleniumConfig config = SeleniumConfig.getConfig();
		GridHubConfiguration hubConfig = config.getHubConfig();
		
		assertTrue(GridUtility.isThisMyIpAddress(InetAddress.getByName(hubConfig.getHost())), "Configured for non-local hub host");
		assertFalse(GridUtility.isHubActive(hubConfig), "Configured local hub should initially be inactive");
		assertTrue(GridUtility.isHubActive(), "Configured local hub should have been activated");
	}

}
