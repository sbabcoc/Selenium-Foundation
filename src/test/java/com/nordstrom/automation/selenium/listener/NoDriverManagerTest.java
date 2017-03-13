package com.nordstrom.automation.selenium.listener;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.listeners.DriverManager;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.ListenerChain;
import com.nordstrom.automation.testng.ListenerChainable;

@Listeners({ListenerChain.class})
public class NoDriverManagerTest implements ListenerChainable {

	@BeforeMethod(groups = {"NoDriverBefore"})
	@NoDriver
	public void beforeMethodNoDriver() {
		Assert.assertNull(DriverManager.getDriver(), "Driver should not have been created");
	}
	
	@Test(groups = {"NoBeforeNoDriver"})
	@NoDriver
	public void testNoBeforeNoDriver() {
		Assert.assertNull(DriverManager.getDriver(), "Driver should not have been created");
	}
	
	@Test(groups = {"NoDriverBefore"})
	public void testNoDriverBefore() {
		Assert.assertNotNull(DriverManager.getDriver(), "Driver should have been created");
	}
	
	@Override
	public void attachListeners(ListenerChain listenerChain) {
		listenerChain.around(DriverManager.class).around(ExecutionFlowController.class);
	}
	
}
