package com.nordstrom.automation.selenium.support;

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
     * If present, get the driver for the current test.
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
     * If present, get the initial page for the current test.
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

}
