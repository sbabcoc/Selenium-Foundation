package com.nordstrom.automation.selenium.interfaces;

import org.openqa.selenium.WebDriver;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;

public interface DriverProvider {
	
	WebDriver provideDriver(IInvokedMethod method, ITestResult testResult);

}
