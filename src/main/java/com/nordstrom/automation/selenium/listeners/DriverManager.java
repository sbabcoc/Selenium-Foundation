package com.nordstrom.automation.selenium.listeners;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriverException;
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
import com.nordstrom.automation.selenium.support.TestBase;
import com.nordstrom.automation.testng.ExecutionFlowController;

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
        if ( ! (testResult.getInstance() instanceof TestBase)) {
            return;
        }
        
        boolean getDriver = false;
        ITestNGMethod testMethod = invokedMethod.getTestMethod();
        Method method = testMethod.getConstructorOrMethod().getMethod();
        TestBase instance = (TestBase) testMethod.getInstance();
        
        // ensure current test result is set
        Reporter.setCurrentTestResult(testResult);
                        
        // get driver supplied by preceding phase
        Optional<WebDriver> optDriver = instance.nabDriver();
        // get @InitialPage from invoked method
        InitialPage initialPage = method.getAnnotation(InitialPage.class);
        
        // if invoked method is @Test
        if (testMethod.isTest()) {
            // get driver if @NoDriver is absent
            getDriver = (null == method.getAnnotation(NoDriver.class));
            
            // if getting a driver
            if (getDriver) {
                // if method lacks @InitialPage and none specified by @BeforeMethod
                if ((initialPage == null) && ! instance.hasInitialPage()) {
                    // get @InitialPage from class that declares invoked method
                    initialPage = method.getDeclaringClass().getAnnotation(InitialPage.class);
                }
            // otherwise, if driver supplied by @BeforeMethod
            } else if (optDriver.isPresent()) {
                // close active driver
                optDriver = closeDriver(testResult);
            }
        // otherwise, if invoked method is @Before...
        } else if (testMethod.isBeforeMethodConfiguration() || testMethod.isBeforeClassConfiguration()
                || testMethod.isBeforeGroupsConfiguration() || testMethod.isBeforeTestConfiguration()
                || testMethod.isBeforeSuiteConfiguration()) {
            // determine if driver is needed
            getDriver = (initialPage != null);
        }
        
        // if getting a driver
        if (getDriver) {
            SeleniumConfig config = SeleniumConfig.getConfig();
            
            // if driver not yet acquired
            if ( ! optDriver.isPresent()) {
                WebDriver driver;
                long prior = System.currentTimeMillis();
                // if test class provides its own drivers
                if (instance instanceof DriverProvider) {
                    driver = ((DriverProvider) instance).provideDriver(invokedMethod, testResult);
                } else {
                    driver = GridUtility.getDriver(testResult);
                }
                
                if (driver != null) {
                    setDriverTimeouts(driver, config);
                    optDriver = instance.setDriver(driver);
                    if (testMethod.isTest()) {
                        long after = System.currentTimeMillis();
                        ExecutionFlowController.adjustTimeout(after - prior, testResult);
                    }
                }
            }
            
            // if driver acquired and initial page specified
            if ((optDriver.isPresent()) && (initialPage != null)) {
                Page page = Page.openInitialPage(initialPage, optDriver.get(), config.getTargetUri());
                instance.setInitialPage(instance.prepInitialPage(page));
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
        ITestNGMethod method = testResult.getMethod();
        if ( ! (method.isTest() || method.isBeforeMethodConfiguration())) {
            // ensure current test result is set
            Reporter.setCurrentTestResult(testResult);
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
        Process gridProc = GridUtility.getGridNode();
        if (gridProc != null) {
            gridProc.destroy();
        }
        
        gridProc = GridUtility.getGridHub();
        if (gridProc != null) {
            gridProc.destroy();
        }
    }

    @Override
    public void onStart(ITestContext testContext) {
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
     * Get the driver for the specified configuration context.
     * 
     * @param testResult configuration context (TestNG test result object)
     * @return (optional) driver from the specified test result
     */
    public static Optional<WebDriver> findDriver(ITestResult testResult) {
        if (testResult.getInstance() instanceof TestBase) {
            // ensure current test result is set
            Reporter.setCurrentTestResult(testResult);
            return ((TestBase) testResult.getInstance()).nabDriver();
        } else {
            return Optional.empty();
        }
    }
    
    /**
     * Determine if a driver is present in the specified configuration context.
     * 
     * @param testResult configuration context (TestNG test result object)
     * @return 'true' if a driver is present; otherwise 'false'
     */
    public static boolean hasDriver(ITestResult testResult) {
        return findDriver(testResult).isPresent();
    }

    /**
     * Close the Selenium driver attached to the specified configuration context.
     * 
     * @param testResult configuration context (TestNG test result object)
     * @return an empty {@link Optional} object
     */
    private static Optional<WebDriver> closeDriver(ITestResult testResult) {
        Optional<WebDriver> optDriver = findDriver(testResult);
        if (optDriver.isPresent()) {
            WebDriver driver = optDriver.get();
            try {
                ((JavascriptExecutor) driver).executeScript("return window.stop");
            } catch (WebDriverException | UnsupportedOperationException e) {
                // Let's make sure our graceful shutdown process doesn't cause failures.
            }
            
            try {
                driver.switchTo().alert().dismiss();
            } catch (WebDriverException e) {
                // The driver throws an exception if no alert is present. This is normal and unavoidable.
            }
            
            driver.quit();
            optDriver = ((TestBase) testResult.getInstance()).setDriver(null);
        }
        
        return optDriver;
    }
}
