package com.nordstrom.automation.selenium.listener;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.listeners.DriverManager;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.ListenerChain;
import com.nordstrom.automation.testng.ListenerChainable;

@Listeners({ListenerChain.class})
public class DriverManagerTest implements ListenerChainable {
	
	@BeforeMethod(groups = {"WithDriverBefore"})
	public void beforeMethodWithDriver() {
		Assert.assertNotNull(DriverManager.getDriver(), "Driver should have been created");
	}
	
	@Test(groups = {"WithDriverBefore"})
	@NoDriver
	public void testWithDriverBefore() {
		System.out.println(Reporter.getCurrentTestResult().getTestContext().getOutputDirectory());
		Assert.assertNotNull(DriverManager.getDriver(), "Driver should have been created");
	}

	@Override
	public void attachListeners(ListenerChain listenerChain) {
		listenerChain.around(DriverManager.class).around(ExecutionFlowController.class);
	}
	
}
