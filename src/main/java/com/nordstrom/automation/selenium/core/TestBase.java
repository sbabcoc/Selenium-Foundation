package com.nordstrom.automation.selenium.core;

import java.lang.reflect.Method;
import org.openqa.selenium.WebDriver;

import com.google.common.base.Optional;
import com.nordstrom.automation.selenium.exceptions.DriverNotAvailableException;
import com.nordstrom.automation.selenium.exceptions.InitialPageNotSpecifiedException;
import com.nordstrom.automation.selenium.model.Page;
import com.nordstrom.automation.selenium.platform.PlatformEnum;
import com.nordstrom.automation.selenium.platform.PlatformTargetable;
import com.nordstrom.common.file.PathUtils.ReportsDirectory;
import com.nordstrom.common.file.PathUtils.PathModifier;

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
    public Page getInitialPage() {
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
    public boolean hasInitialPage() {
        return nabInitialPage().isPresent();
    }
    
    /**
     * If present, get the initial page for the current test. <br>
     * <b>NOTE</b>: It's uncommon that you'll need to access this {@link Optional} value directly. You'll typically
     * use the {@link #getInitialPage} and {@link #hasInitialPage} methods instead.
     * 
     * @return (optional) initial page for the current test
     */
    public abstract Optional<Page> nabInitialPage();
    
    /**
     * Set the initial page for the current test.
     * 
     * @param pageObj initial page for the current test
     */
    public abstract void setInitialPage(Page pageObj);
    
    /**
     * Get scenario-specific path modifier for {@link ReportsDirectory#getPathForObject(Object)}.
     * <p>
     * <b>NOTE</b>: This method is declared in the {@link PathModifier} interface, which is extended
     * by {@link PlatformTargetable}. This method provides a default implementation for test classes
     * that extend {@link TestBase} and implement <b>PlatformTargetable</b>.
     * 
     * @return scenario-specific path modifier
     * @see PathModifier#getSubPath()
     */
    @SuppressWarnings("unchecked")
    public <P extends Enum<?> & PlatformEnum> String[] getSubPath() {
        String[] subPath = {};
        if (this instanceof PlatformTargetable) {
            P targetPlatform = ((PlatformTargetable<P>) this).getTargetPlatform();
            if (targetPlatform != null) {
                subPath = new String[] {targetPlatform.getName()};
            }
        }
        return subPath;
    }
    
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
     * @param driver WebDriver object
     */
    public <P extends Enum<?> & PlatformEnum> void activatePlatform(WebDriver driver) {
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
            return Optional.absent();
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
}
