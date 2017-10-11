package com.nordstrom.automation.selenium.core;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriverException;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.SeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.annotations.PageUrl;
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
public class DriverManager {

    private DriverManager() {
        throw new AssertionError("DriverManager is a static utility class that cannot be instantiated");
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
     * @param obj test class instance
     * @param method test method
     */
    public static void beforeInvocation(Object obj, Method method) {
        if ( ! (obj instanceof TestBase)) {
            return;
        }
        
        boolean getDriver = false;
        TestBase instance = (TestBase) obj;
        
        // get driver supplied by preceding phase
        Optional<WebDriver> optDriver = instance.nabDriver();
        // get @InitialPage from invoked method
        InitialPage initialPage = method.getAnnotation(InitialPage.class);
        
        // if invoked method is @Test
        if (instance.isTest(method)) {
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
                optDriver = closeDriver(instance);
            }
        // otherwise, if invoked method is @Before...
        } else if (instance.isBeforeMethod(method) || instance.isBeforeClass(method)) {
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
                    driver = ((DriverProvider) instance).provideDriver(instance, method);
                } else {
                    driver = GridUtility.getDriver();
                }
                
                if (driver != null) {
                    setDriverTimeouts(driver, config);
                    optDriver = instance.setDriver(driver);
                    if (instance.isTest(method)) {
                        long after = System.currentTimeMillis();
                        instance.adjustTimeout(after - prior);
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
     * @param obj test class instance
     * @param method test method
     */
    public static void afterInvocation(Object obj, Method method) {
        if (obj instanceof TestBase) {
            TestBase instance = (TestBase) obj;
            if ( ! (instance.isTest(method) || instance.isBeforeMethod(method))) {
                closeDriver(instance);
            }
        }
    }

    /**
     * Perform post-suite processing:
     * <ul>
     *     <li>If a Selenium Grid node process was spawned, shut it down.</li>
     *     <li>If a Selenium Grid hub process was spawned, shut it down.</li>
     * </ul>
     */
    public static void onFinish() {
        GridUtility.stopGridNode(true);
        GridUtility.stopGridHub(true);
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
     * If present, get the driver for the specified test class instance.
     * 
     * @param obj test class instance
     * @return (optional) driver from the specified test result
     */
    public static Optional<WebDriver> nabDriver(Object obj) {
        if (obj instanceof TestBase) {
            return ((TestBase) obj).nabDriver();
        } else {
            return Optional.empty();
        }
    }
    
    /**
     * Determine if a driver is present in the specified test class instance.
     * 
     * @param obj test class instance
     * @return 'true' if a driver is present; otherwise 'false'
     */
    public static boolean hasDriver(Object obj) {
        return nabDriver(obj).isPresent();
    }

    /**
     * Close the Selenium driver attached to the specified test class instance.
     * 
     * @param obj test class instance
     * @return an empty {@link Optional} object
     */
    public static Optional<WebDriver> closeDriver(Object obj) {
        Optional<WebDriver> optDriver = nabDriver(obj);
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
            optDriver = ((TestBase) obj).setDriver(null);
        }
        
        return optDriver;
    }
}
