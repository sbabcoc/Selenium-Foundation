package com.nordstrom.automation.selenium.core;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Supplier;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.exceptions.DriverNotAvailableException;
import com.nordstrom.automation.selenium.exceptions.InitialPageNotSpecifiedException;
import com.nordstrom.automation.selenium.model.Page;

/**
 * This interface defines the contract for Selenium Foundation test classes.
 */
public abstract class TestBase {

    /**
     * Get the driver for the current test.
     * 
     * @return driver for the current test
     * @throws DriverNotAvailableException No driver was found in the current test context
     */
    public WebDriver getDriver() {
        Optional<WebDriver> optDriver = nabDriver();
        if (optDriver.isPresent()) {
            return optDriver.get();
        }
        throw new DriverNotAvailableException();
    }
    
    /**
     * Determine if the current test has a driver.
     * 
     * @return 'true' if a driver is present; otherwise 'false'
     */
    public boolean hasDriver() {
        return nabDriver().isPresent();
    }
    
    /**
     * If present, get the driver for the current test. <br>
     * <b>NOTE</b>: It's uncommon that you'll need to access this {@link Optional} value directly. You'll typically
     * use the {@link #getDriver} and {@link #hasDriver} methods instead.
     * 
     * @return (optional) driver for the current test
     */
    public abstract Optional<WebDriver> nabDriver();
    
    /**
     * Set the driver for the current test.
     * 
     * @param driver driver for the current test; 'null' to discard driver
     */
    public abstract void setDriver(WebDriver driver);
    
    /**
     * Prepare the specified page object for use:
     * <ul>
     *     <li>Ensure that the page object is associated with a window handle.</li>
     *     <li>Set the driver for the current test to the page object driver.</li>
     *     <li>Return an enhanced instance of the page object.</li>
     * </ul>
     * 
     * @param pageObj page object to be prepared
     * @return prepared page object
     */
    public Page prepInitialPage(Page pageObj) {
        if (pageObj.getWindowHandle() == null) {
            try {
                pageObj.setWindowHandle(pageObj.getWrappedDriver().getWindowHandle());
            } catch (WebDriverException ignored) {
                // nothing to do here
            }
        }
        // required when initial page is local file
        setDriver(pageObj.getWrappedDriver());
        return pageObj.enhanceContainer(pageObj);
    }
    
    /**
     * Get the initial page for the current test.
     * 
     * @param <T> subclass of {@link Page}
     * @return initial page for the current test
     * @throws InitialPageNotSpecifiedException No initial page has been specified
     */
    public <T extends Page> T getInitialPage() {
        Optional<T> optInitialPage = nabInitialPage();
        if (optInitialPage.isPresent()) {
            return optInitialPage.get();
        }
        throw new InitialPageNotSpecifiedException();
    }
    
    /**
     * Determine if the current test has specified an initial page.
     * 
     * @return 'true' if an initial page has been specified; otherwise 'false'
     */
    public boolean hasInitialPage() {
        return nabInitialPage().isPresent();
    }
    
    /**
     * Skip this test if target browser lacks Shadow DOM support.
     */
    public void skipIfNoShadowDom() {
        // if running HtmlUnit
        if ("htmlunit".equals(WebDriverUtils.getBrowserName(getDriver()))) {
            skipTest("This scenario is unsupported on HtmlUnit");
        }
        // if running Safari in Selenium 3
        if ((SeleniumConfig.getConfig().getVersion() == 3)
                && "Safari".equals(WebDriverUtils.getBrowserName(getDriver()))) {
            skipTest("This scenario is unsupported on Safari in Selenium 3");
        }
    }
    
    /**
     * Skip this test if running iOS Safari.
     */
    public void skipIfSafariOnIOS() {
        // if running Safari on iOS via XCUITest
        if ("XCUITest".equals(WebDriverUtils.getAutomationEngine(getDriver()))
                && "Safari".equals(WebDriverUtils.getBrowserName(getDriver()))) {
            skipTest("This scenario is unsupported on iOS Safari");
        }
    }
    
    /**
     * If present, get the initial page for the current test. <br>
     * <b>NOTE</b>: It's uncommon that you'll need to access this {@link Optional} value directly. You'll typically
     * use the {@link #getInitialPage} and {@link #hasInitialPage} methods instead.
     * 
     * @param <T> subclass of {@link Page}
     * @return (optional) initial page for the current test
     */
    public abstract <T extends Page> Optional<T> nabInitialPage();
    
    /**
     * Set the initial page for the current test.
     * 
     * @param <T> subclass of {@link Page}
     * @param pageObj initial page for the current test
     */
    public abstract <T extends Page> void setInitialPage(T pageObj);
    
    /**
     * Get test run output directory.
     * 
     * @return test run output directory
     */
    public abstract String getOutputDirectory();
    
    /**
     * Adjust test method timeout by adding the specified interval.
     * 
     * @param adjust timeout adjustment
     */
    public void adjustTimeout(long adjust) {
        // by default, do nothing
    }
    
    /**
     * Activate the resolved target platform.
     * 
     * @param driver WebDriver object (may be {@code null})
     */
    public void activatePlatform(WebDriver driver) {
        // by default, do nothing
    }
    
    /**
     * Wrap the specified object in an {@link Optional} object.
     * 
     * @param <T> type of object to be wrapped
     * @param obj object to be wrapped (may be 'null')
     * @return (optional) wrapped object; empty if {@code obj} is 'null'
     */
    public static <T> Optional<T> optionalOf(T obj) {
        if (obj != null) {
            return Optional.of(obj);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Invokes the specified supplier, returning its result wrapped in an {@link Optional}.
     * If the supplier throws any exception or returns {@code null}, an empty {@link Optional}
     * is returned instead.
     *
     * @param <T> the type of result supplied
     * @param command the supplier to invoke
     * @return an {@link Optional} containing the supplier's result, or empty if the supplier
     *         threw an exception or returned {@code null}
     */
    public static <T> Optional<T> invokeSafely(Supplier<T> command) {
        try {
            return Optional.ofNullable(command.get());
        } catch (Throwable t) {
            return Optional.empty();
        }
    }
    
    /**
     * Determine if the specified method is a 'test' method.
     * 
     * @param method method to be checked
     * @return 'true' if specified method has {@code Test} annotation; otherwise 'false'
     */
    public abstract boolean isTest(Method method);
    
    /**
     * Determine if the specified method is a 'before method' configuration method.
     * 
     * @param method method to be checked
     * @return 'true' if specified method has {@code Before} annotation; otherwise 'false'
     */
    public abstract boolean isBeforeMethod(Method method);
    
    /**
     * Determine if the specified method is an 'after method' configuration method.
     * 
     * @param method method to be checked
     * @return 'true' if specified method has {@code After} annotation; otherwise 'false'
     */
    public abstract boolean isAfterMethod(Method method);
    
    /**
     * Determine if the specified method is a 'before class' configuration method.
     * 
     * @param method method to be checked
     * @return 'true' if specified method has {@code BeforeClass} annotation; otherwise 'false'
     */
    public abstract boolean isBeforeClass(Method method);
    
    /**
     * Determine if the specified method is a 'after class' configuration method.
     * 
     * @param method method to be checked
     * @return 'true' if specified method has {@code AfterClass} annotation; otherwise 'false'
     */
    public abstract boolean isAfterClass(Method method);
    
    /**
     * Skip the test that's about to executed.
     * 
     * @param message message for the framework-specific test-skip exception
     * @throws RuntimeException framework-specific exception throw to skip the test
     */
    public abstract void skipTest(String message) throws RuntimeException;
}
