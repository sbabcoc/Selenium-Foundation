package com.nordstrom.automation.selenium.core;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.testng.annotations.Test;

public class GridUtilityTest {
	
	@Test
	public void testIsActive() throws UnknownHostException, MalformedURLException {
		GridUtility.isHubActive();
		GridUtility.isHubActive();
	}

}
