package com.nordstrom.automation.selenium.listener;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.listeners.DriverManager;
import com.nordstrom.automation.testng.ExecutionFlowController;

@Listeners({ExecutionFlowController.class, DriverManager.class})
public class DriverManagerTest {
	
	@BeforeMethod
	@NoDriver
	public void beforeMethod() {
		boolean foo = true;
	}
	
	@Test
	@NoDriver
	public void foo() {
		boolean foo = true;
	}
	
	@Test
	public void bar() {
		boolean bar = true;
		DriverManager.getDriver().close();
	}

}
