package com.nordstrom.automation.selenium.listeners;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.interfaces.DriverProvider;

public class DriverManager implements IInvokedMethodListener, ITestListener {

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
		if (testResult == null) throw new NullPointerException("Test result object must be non-null");
		return (WebDriver) testResult.getAttribute(DRIVER);
	}
	
	/**
	 * Set the driver for the specified test result
	 * 
	 * @param driver test result object
	 * @param testResult driver for the specified test result
	 */
	public static void setDriver(WebDriver driver, ITestResult testResult) {
		if (testResult == null) throw new NullPointerException("Test result object must be non-null");
		testResult.setAttribute(DRIVER, driver);
	}
	
	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
		ITestNGMethod testMethod = method.getTestMethod();
		if (testMethod.isTest() || testMethod.isBeforeMethodConfiguration()) {
			WebDriver driver = getDriver(testResult);
			if (driver == null) {
				NoDriver noDriver = testMethod.getConstructorOrMethod().getMethod().getAnnotation(NoDriver.class);
				if (noDriver == null) {
					Object instance = testMethod.getInstance();
					if (instance instanceof DriverProvider) {
						driver = ((DriverProvider) instance).provideDriver(method, testResult);
					} else {
						driver = GridUtility.getDriver(testResult);
					}
					setDriver(driver, testResult);
				}
			}
		}
	}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
		// no post-invocation processing
	}

	@Override
	public void onFinish(ITestContext testContext) {
		Process gridProc = GridUtility.getGridNode(testContext);
		if (gridProc != null) {
			gridProc.destroy();
		}
		
		gridProc = GridUtility.getGridHub(testContext);
		if (gridProc != null) {
			gridProc.destroy();
		}
	}

	@Override
	public void onStart(ITestContext paramITestContext) {
		// no pre-run processing
		
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult testResult) {
		closeDriver(testResult);
	}

	@Override
	public void onTestFailure(ITestResult testResult) {
		closeDriver(testResult);
	}

	@Override
	public void onTestSkipped(ITestResult testResult) {
		closeDriver(testResult);
	}

	@Override
	public void onTestStart(ITestResult testResult) {
		// no pre-test processing
	}

	@Override
	public void onTestSuccess(ITestResult testResult) {
		closeDriver(testResult);
	}
	
	/**
	 * Close the Selenium driver attached to the specified configuration context.<br>
	 * 
	 * @param testResult configuration context (TestNG test result object)
	 */
	private void closeDriver(ITestResult testResult) {
		WebDriver driver = getDriver(testResult);
		if (driver != null) {
			try {
				((JavascriptExecutor) driver).executeScript("return window.stop");
			} catch (Exception e) { }
			
			try {
				driver.switchTo().alert().dismiss();
			} catch (Exception e) { }
			
			driver.quit();
		}
	}
}
