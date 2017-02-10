package com.nordstrom.automation.selenium.listener;

import org.openqa.selenium.WebDriver;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.nordstrom.automation.selenium.DriverProvider;
import com.nordstrom.automation.selenium.annotation.NoDriver;

public class DriverManager implements IInvokedMethodListener {

	private static final String DRIVER = "DRIVER";
	
	/**
	 * Get the driver for the current test
	 * 
	 * @return driver for the current test
	 */
	public static WebDriver getDriver() {
		return getDriver(Reporter.getCurrentTestResult());
	}
	
	/**
	 * Set the driver for the current test
	 * 
	 * @param driver driver for the current test
	 */
	public static void setDriver(WebDriver driver) {
		setDriver(driver, Reporter.getCurrentTestResult());
	}
	
	/**
	 * Get the driver for the specified test result
	 * 
	 * @param testResult test result object
	 * @return driver from the specified test result
	 */
	public static WebDriver getDriver(ITestResult testResult) {
		return (WebDriver) testResult.getAttribute(DRIVER);
	}
	
	/**
	 * Set the driver for the specified test result
	 * 
	 * @param driver test result object
	 * @param testResult driver for the specified test result
	 */
	public static void setDriver(WebDriver driver, ITestResult testResult) {
		testResult.setAttribute(DRIVER, driver);
	}

	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
		WebDriver driver = getDriver(testResult);
		if (driver == null) {
			NoDriver noDriver = method.getTestMethod().getConstructorOrMethod().getMethod().getAnnotation(NoDriver.class);
			if (noDriver == null) {
				Object instance = method.getTestMethod().getInstance();
				if (instance instanceof DriverProvider) {
					driver = ((DriverProvider) instance).provideDriver(method, testResult);
				} else {
					
				}
				setDriver(driver, testResult);
			}
		}
	}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
		// no post-invocation processing
	}

}
