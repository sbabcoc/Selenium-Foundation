package com.nordstrom.automation.selenium.listeners;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Timeouts;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.SeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.annotations.PageUrl;
import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.interfaces.DriverProvider;
import com.nordstrom.automation.selenium.model.Page;

/**
 * This TestNG listener performs several basic functions related to driver session management:
 * <ul>
 *     <li>Manage Selenium driver lifetime.</li>
 *     <li>For local execution, manage a local instance of Selenium Grid.</li>
 *     <li>Store and dispense the driver instance created for the test.</li>
 *     <li>Manage configured driver timeout intervals.</li>
 *     <li>If an initial page class is specified:
 *         <ul>
 *             <li>Open the initial page based on its {@link PageUrl} annotation.</li>
 *             <li>Store the page object for subsequent dispensing to the test.</li>
 *         </ul>
 *     </li>
 * </ul>
 * 
 * @see GridUtility
 */
public class DriverManager implements IInvokedMethodListener, ITestListener {

	private static final String DRIVER = "Driver";
	private static final String CLOSE_DRIVER = "CloseDriver";
	private static final String INITIAL_PAGE = "InitialPage";
	
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
 	 * @param testResult configuration context (TestNG test result object)
	 * @return driver from the specified test result
	 */
	public static WebDriver getDriver(ITestResult testResult) {
		validateTestResult(testResult);
		return (WebDriver) testResult.getAttribute(DRIVER);
	}
	
	/**
	 * Set the driver for the specified test result
	 * 
	 * @param driver driver for the specified test result
	 * @param testResult configuration context (TestNG test result object)
	 */
	public static void setDriver(WebDriver driver, ITestResult testResult) {
		validateTestResult(testResult);
		if (driver != null) {
			testResult.setAttribute(DRIVER, driver);
		} else {
			testResult.removeAttribute(DRIVER);
		}
	}
	
	/**
	 * Set the initial page object for the current test
	 * 
	 * @param pageObj page object for the current test
	 */
	public static void setInitialPage(Page pageObj) {
		setInitialPage(pageObj, Reporter.getCurrentTestResult());
	}
	
	/**
	 * Get the initial page object for the current test
	 * 
	 * @return page object for the current test
	 */
	public static Page getInitialPage() {
		return (Page) getInitialPage(Reporter.getCurrentTestResult());
	}
	
	/**
	 * Set the initial page object for the specified test result
	 * 
	 * @param pageObj page object for the specified test result
	 * @param testResult configuration context (TestNG test result object)
	 */
	public static void setInitialPage(Page pageObj, ITestResult testResult) {
		validateTestResult(testResult);
		if (pageObj.getWindowHandle() == null) {
			pageObj.setWindowHandle(pageObj.getDriver().getWindowHandle());
		}
		testResult.setAttribute(INITIAL_PAGE, pageObj.enhanceContainer(pageObj));
		setDriver(pageObj.getDriver(), testResult);
	}
	
	/**
	 * Get the initial page object for the specified test result
	 * 
	 * @param testResult configuration context (TestNG test result object)
	 * @return page object for the specified test result
	 */
	public static Page getInitialPage(ITestResult testResult) {
		validateTestResult(testResult);
		return (Page) testResult.getAttribute(INITIAL_PAGE);
	}
	
	/**
	 * Perform pre-invocation processing:
	 * <ul>
	 *     <li>Ensure that a driver instance has been created for the test.</li>
	 *     <li>Store the driver instance for subsequent dispensing.</li>
	 *     <li>Manage configured driver timeout intervals.</li>
	 *     <li>If specified, open the initial page, storing the page object for subsequent dispensing.</li>
	 * </ul>
	 * 
	 * @param invokedMethod an object representing the method that's about to be invoked
	 * @param testResult test result object for the method that's about to be invoked
	 */
	@Override
	public void beforeInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
		boolean getDriver = false;
		ITestNGMethod testMethod = invokedMethod.getTestMethod();
		Method method = testMethod.getConstructorOrMethod().getMethod();
		
		// get driver supplied by preceding phase
		WebDriver driver = getDriver(testResult);
		// get @InitialPage from invoked method
		InitialPage initialPage = method.getAnnotation(InitialPage.class);
		
		// if invoked method is @Test
		if (testMethod.isTest()) {
			// get driver if @NoDriver is absent
			getDriver = (null == method.getAnnotation(NoDriver.class));
			
			// if getting a driver
			if (getDriver) {
				// if method lacks @InitialPage and none specified by @BeforeMethod
				if ((initialPage == null) && (getInitialPage(testResult) == null)) {
					// get @InitialPage from class that declares invoked method
					initialPage = method.getDeclaringClass().getAnnotation(InitialPage.class);
				}
			// otherwise, if driver supplied by @BeforeMethod
			} else if (driver != null) {
				// close active driver
				closeDriver(testResult);
				// dump reference
				driver = null;
			}
		// otherwise, if invoked method is @Before...
		} else if (testMethod.isBeforeMethodConfiguration() || testMethod.isBeforeClassConfiguration()
				|| testMethod.isBeforeGroupsConfiguration() || testMethod.isBeforeTestConfiguration()
				|| testMethod.isBeforeSuiteConfiguration()) {
			// determine if driver is needed
			getDriver = (initialPage != null);
			// if getting a driver and invoked method isn't @BeforeMethod, close it after invocation
			if (getDriver && (!testMethod.isBeforeMethodConfiguration())) testResult.setAttribute(CLOSE_DRIVER, "");
		}
		
		// if getting a driver
		if (getDriver) {
			SeleniumConfig config = SeleniumConfig.getConfig(testResult);
			
			// if driver not yet acquired
			if (driver == null) {
				Object instance = testMethod.getInstance();
				// if test class supplied its own drivers
				if (instance instanceof DriverProvider) {
					driver = ((DriverProvider) instance).provideDriver(invokedMethod, testResult);
				} else {
					driver = GridUtility.getDriver(testResult);
				}
				setDriverTimeouts(driver, config);
				setDriver(driver, testResult);
			}
			
			// if driver acquired and initial page specified
			if ((driver != null) && (initialPage != null)) {
				Page page = Page.openInitialPage(initialPage, driver, config.getTargetUri());
				setInitialPage(page, testResult);
			}
		}
	}

	/**
	 * Perform post-invocation processing:
	 * <ul>
	 *     <li>If indicated, close the driver that was acquired for this method.</li>
	 * </ul>
	 * 
	 * @param invokedMethod an object representing the method that's just been invoked
	 * @param testResult test result object for the method that's just been invoked
	 */
	@Override
	public void afterInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
		if (testResult.getAttribute(CLOSE_DRIVER) != null) {
			closeDriver(testResult);
		}
	}

	/**
	 * Perform post-suite processing:
	 * <ul>
	 *     <li>If a Selenium Grid node process was spawned, shut it down.</li>
	 *     <li>If a Selenium Grid hub process was spawned, shut it down.</li>
	 * </ul>
	 * 
	 * @param testContext execution context for the test suite that just finished
	 */
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
	 * Set configured timeout intervals in the specified driver.
	 * 
	 * @param driver driver object in which to configure timeout intervals
	 * @param config configuration object that specifies timeout intervals
	 */
	public static void setDriverTimeouts(WebDriver driver, SeleniumConfig config) {
		Timeouts timeouts = driver.manage().timeouts();
		timeouts.setScriptTimeout(WaitType.SCRIPT.getInterval(config), TimeUnit.SECONDS);
		timeouts.implicitlyWait(WaitType.IMPLIED.getInterval(config), TimeUnit.SECONDS);
		timeouts.pageLoadTimeout(WaitType.PAGE_LOAD.getInterval(config), TimeUnit.SECONDS);
	}

	/**
	 * Close the Selenium driver attached to the specified configuration context.
	 * 
	 * @param testResult configuration context (TestNG test result object)
	 */
	private void closeDriver(ITestResult testResult) {
		WebDriver driver = getDriver(testResult);
		if (driver != null) {
			try {
				((JavascriptExecutor) driver).executeScript("return window.stop");
			} catch (Exception e) {
				// Let's make sure our graceful shutdown process doesn't cause failures.
			}
			
			try {
				driver.switchTo().alert().dismiss();
			} catch (Exception e) {
				// The driver throws an exception if no alert is present. This is normal and unavoidable.
			}
			
			driver.quit();
			setDriver(null, testResult);
		}
	}
	
	/**
	 * Verify that the specified configuration context is non-null.
	 * 
	 * @param testResult configuration context (TestNG test result object)
	 */
	private static void validateTestResult(ITestResult testResult) {
		if (testResult == null) throw new NullPointerException("Test result object must be non-null");
	}
}
