package com.nordstrom.automation.selenium.core;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.ui.FluentWait;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.SeleniumConfig;
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
public final class DriverManager {

    /**
     * Private constructor to prevent instantiation.
     */
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
    @SuppressWarnings({"squid:S3776", "squid:MethodCyclomaticComplexity"})
    public static void beforeInvocation(final Object obj, final Method method) {
        if (!(obj instanceof TestBase)) {
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
                if ((initialPage == null) && !instance.hasInitialPage()) {
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
            if (!optDriver.isPresent()) {
                long prior = System.currentTimeMillis();
                
                long timeOutInSeconds = config.getLong(SeleniumSettings.HOST_TIMEOUT.key());
                DriverSessionWait wait = new DriverSessionWait(instance, timeOutInSeconds);
                wait.ignoring(WebDriverException.class);
                WebDriver driver = wait.until(driverIsAcquired(method));
                
                setDriverTimeouts(driver, config);
                instance.setDriver(driver);
                optDriver = Optional.of(driver);
                if (instance.isTest(method)) {
                    long after = System.currentTimeMillis();
                    instance.adjustTimeout(after - prior);
                    instance.activatePlatform(driver);
                }
            }
            
            // if initial page spec'd
            if (initialPage != null) {
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
    public static void afterInvocation(final Object obj, final Method method) {
        if (obj instanceof TestBase) {
            TestBase instance = (TestBase) obj;
            if (!(instance.isTest(method) || instance.isBeforeMethod(method))) {
                closeDriver(instance);
            }
        }
    }

    /**
     * Perform post-suite processing, shutting down the local Selenium Grid.
     */
    public static void onFinish() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        if (config.getBoolean(SeleniumSettings.SHUTDOWN_GRID.key())) {
            try {
                config.shutdownGrid(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Set configured timeout intervals in the specified driver.
     * 
     * @param driver driver object in which to configure timeout intervals
     * @param config configuration object that specifies timeout intervals
     */
    public static void setDriverTimeouts(final WebDriver driver, final SeleniumConfig config) {
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
    public static Optional<WebDriver> nabDriver(final Object obj) {
        if (obj instanceof TestBase) {
            return ((TestBase) obj).nabDriver();
        } else {
            return Optional.absent();
        }
    }
    
    /**
     * Determine if a driver is present in the specified test class instance.
     * 
     * @param obj test class instance
     * @return 'true' if a driver is present; otherwise 'false'
     */
    public static boolean hasDriver(final Object obj) {
        return nabDriver(obj).isPresent();
    }
    
    /**
     * Get the remote session ID of the specified driver.<br>
     * <b>NOTE</b>: The session ID will be 'null' if no remote session is associated with the specified driver.<br>
     * <b>NOTE</b>: If the specified driver isn't a {@link RemoteWebDriver}, an empty {@link Optional} is returned.
     * 
     * @param driver driver object
     * @return optional session ID (see NOTES) 
     */
    public static Optional<SessionId> getSessionId(final WebDriver driver) {
        if (driver instanceof RemoteWebDriver) {
            SessionId sessionId = ((RemoteWebDriver) driver).getSessionId();
            return Optional.of(sessionId);
        }
        return Optional.absent();
    }

    /**
     * Close the Selenium driver attached to the specified test class instance.
     * 
     * @param obj test class instance
     * @return an empty {@link Optional} object
     */
    public static Optional<WebDriver> closeDriver(final Object obj) {
        Optional<WebDriver> optDriver = nabDriver(obj);
        if (optDriver.isPresent()) {
            WebDriver driver = optDriver.get();
            
            try {
                ((JavascriptExecutor) driver).executeScript("return window.stop");
            } catch (WebDriverException | UnsupportedOperationException e) { //NOSONAR
                // Let's make sure our graceful shutdown process doesn't cause failures.
            }
            
            try {
                driver.switchTo().alert().dismiss();
            } catch (WebDriverException e) { //NOSONAR
                // The driver throws an exception if no alert is present. This is normal and unavoidable.
            }
            
            ((TestBase) obj).setInitialPage(null);
            ((TestBase) obj).setDriver(null);
            optDriver = Optional.absent();
            driver.quit();
        }
        
        return optDriver;
    }
    
    /**
     * Returns a 'wait' proxy that acquires a driver session.
     * 
     * @param method test method
     * @return new driver session
     * @throws WebDriverException If acquisition attempt fails.
     */
    private static Function<TestBase, WebDriver> driverIsAcquired(final Method method) {
        return new Function<TestBase, WebDriver>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public WebDriver apply(final TestBase instance) {
                // if test class provides its own drivers
                if (instance instanceof DriverProvider) {
                    return ((DriverProvider) instance).provideDriver(method);
                } else {
                    return GridUtility.getDriver();
                }
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "driver to be aquired";
            }
        };
    }
    
    /**
     * This class extends {@link FluentWait}, specifying {@link TestBase} as the type parameter. This 'wait' object
     * will repeatedly apply specified 'wait' proxies until they complete successfully or the specified timeout has
     * expired, pausing 500 mS between iterations.
     */
    public static class DriverSessionWait extends FluentWait<TestBase> {
        
        /**
         * Constructor for driver session 'wait' object
         * 
         * @param context Selenium Foundation test class object
         * @param timeOutInSeconds 'wait' timeout in seconds
         */
        public DriverSessionWait(final TestBase context, final long timeOutInSeconds) {
            super(context);
            withTimeout(timeOutInSeconds, TimeUnit.SECONDS);
        }
    }
}
