package com.nordstrom.automation.selenium.interfaces;

import org.openqa.selenium.WebDriver;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;

public interface DriverProvider {
	
	/**
	 * Acquire a driver object for the specified method.
	 * 
	 * @param method representation of the method being invoked
	 * @param testResult configuration context (TestNG test result object)
	 * @return driver object
	 */
	WebDriver provideDriver(IInvokedMethod method, ITestResult testResult);

}
