package com.nordstrom.automation.selenium.interfaces;

import org.openqa.selenium.WebDriver;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;

/**
 * Test classes with non-standard driver configurations implement this interface, which enables the driver manager 
 * to obtain a driver from the {@link #provideDriver(IInvokedMethod, ITestResult)} method of test class instance.
 */
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
