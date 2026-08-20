package com.nordstrom.automation.selenium.core;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.interfaces.WrapsContext;

/**
 * Minimal {@link WrapsContext} implementation representing a driver as its own top-level search context.
 * <p>
 * Used when wrapping elements found by a script executed directly against the driver, rather than scoped to
 * any particular {@code ComponentContainer} - there's no page object in the picture at that point, so this
 * stands in as the simplest valid context. Always wraps the real, unwrapped driver (never the auto-wrapping
 * proxy built by {@link RobustDriverFactory}), so that a wrapped element's own refresh calls go straight to
 * the driver without looping back through the interceptor a second time.
 * <p>
 * Implements {@link SearchContext} directly, not just {@link WrapsContext} - matching
 * {@code ComponentContainer}'s own {@code implements SearchContext, WrapsContext} - because
 * {@code RobustElementWrapper.refreshReference} casts its {@code WrapsContext} field straight to
 * {@code SearchContext} rather than going through {@code getWrappedContext()}. Every other
 * {@code WrapsContext} implementation in this codebase already satisfies that assumption; a
 * {@code WrapsContext} that doesn't implement {@code SearchContext} directly throws a
 * {@code ClassCastException} there.
 */
final class DriverContext implements SearchContext, WrapsContext {

    private final WebDriver driver;
    private final long acquiredAt = System.currentTimeMillis();

    /**
     * Constructor for a driver-as-context wrapper.
     * 
     * @param driver the real, unwrapped driver this context represents
     */
    DriverContext(final WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public WebElement findElement(final By by) {
        return driver.findElement(by);
    }

    @Override
    public List<WebElement> findElements(final By by) {
        return driver.findElements(by);
    }

    @Override
    public WebDriver getWrappedDriver() {
        return driver;
    }

    @Override
    public SearchContext getWrappedContext() {
        return driver;
    }

    @Override
    public SearchContext switchTo() {
        driver.switchTo().defaultContent();
        return driver;
    }

    @Override
    public SearchContext switchToParentFrame() {
        return driver;
    }

    @Override
    public SearchContext refreshContext(final long expiration) {
        return driver;
    }

    @Override
    public long acquiredAt() {
        return acquiredAt;
    }
}
