package com.nordstrom.automation.selenium.support;

import java.lang.reflect.Method;
import java.util.Optional;

import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.exceptions.DriverNotAvailableException;
import com.nordstrom.automation.selenium.exceptions.InitialPageNotSpecifiedException;
import com.nordstrom.automation.selenium.model.Page;

/**
 * This interface defines the contract for Selenium Foundation test classes.
 */
public interface TestBase {

    /**
     * Get the driver for the current test.
     * 
     * @return driver for the current test
     * @throws DriverNotAvailableException No driver was found in the current test context
     */
    default WebDriver getDriver() {
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
    default boolean hasDriver() {
        return nabDriver().isPresent();
    }
    
    /**
     * If present, get the driver for the current test. <br>
     * <b>NOTE</b>: It's uncommon that you'll need to access this {@link Optional} value directly. You'll typically
     * use the {@link #getDriver} and {@link #hasDriver} methods instead.
     * 
     * @return (optional) driver for the current test
     */
    Optional<WebDriver> nabDriver();
    
    /**
     * Set the driver for the current test.
     * 
     * @param driver driver for the current test; 'null' to discard driver
     * @return (optional) driver for the current test
     */
    Optional<WebDriver> setDriver(WebDriver driver);
    
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
    default Page prepInitialPage(Page pageObj) {
        if (pageObj.getWindowHandle() == null) {
            pageObj.setWindowHandle(pageObj.getDriver().getWindowHandle());
        }
        // required when initial page is local file
        setDriver(pageObj.getDriver());
        return pageObj.enhanceContainer(pageObj);
    }
    
    /**
     * Get the initial page for the current test.
     * 
     * @return initial page for the current test
     * @throws InitialPageNotSpecifiedException No initial page has been specified
     */
    default Page getInitialPage() {
        Optional<Page> optInitialPage = nabInitialPage();
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
    default boolean hasInitialPage() {
        return nabInitialPage().isPresent();
    }
    
    /**
     * If present, get the initial page for the current test. <br>
     * <b>NOTE</b>: It's uncommon that you'll need to access this {@link Optional} value directly. You'll typically
     * use the {@link #getInitialPage} and {@link #hasInitialPage} methods instead.
     * 
     * @return (optional) initial page for the current test
     */
    Optional<Page> nabInitialPage();
    
    /**
     * Set the initial page for the current test.
     * 
     * @param pageObj initial page for the current test
     * @return (optional) initial page for the current test
     */
    Optional<Page> setInitialPage(Page pageObj);
    
    /**
     * Wrap the specified object in an {@link Optional} object.
     * 
     * @param obj object to be wrapped (may be 'null')
     * @return (optional) wrapped object; empty if {@code obj} is 'null'
     */
    static <T> Optional<T> optionalOf(T obj) {
        if (obj != null) {
            return Optional.of(obj);
        } else {
            return Optional.empty();
        }
    }
    
    /**
     * Determine if the specified method is a 'test' method.
     * 
     * @param method method to be checked
     * @return 'true' if specified method has {@code Test} annotation; otherwise 'false'
     */
    boolean isTest(Method method);
    
    /**
     * Determine if the specified method is a 'before method' configuration method.
     * 
     * @param method method to be checked
     * @return 'true' if specified method has {@code Before} annotation; otherwise 'false'
     */
    boolean isBeforeMethod(Method method);
    
    /**
     * Determine if the specified method is an 'after method' configuration method.
     * 
     * @param method method to be checked
     * @return 'true' if specified method has {@code After} annotation; otherwise 'false'
     */
    boolean isAfterMethod(Method method);
    
    /**
     * Determine if the specified method is a 'before class' configuration method.
     * 
     * @param method method to be checked
     * @return 'true' if specified method has {@code BeforeClass} annotation; otherwise 'false'
     */
    boolean isBeforeClass(Method method);
    
    /**
     * Determine if the specified method is a 'after class' configuration method.
     * 
     * @param method method to be checked
     * @return 'true' if specified method has {@code AfterClass} annotation; otherwise 'false'
     */
    boolean isAfterClass(Method method);
}
