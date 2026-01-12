package com.nordstrom.automation.selenium.junit;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import com.nordstrom.automation.junit.AtomIdentity;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.WebDriver;
import com.nordstrom.automation.junit.ArtifactParams;
import com.nordstrom.automation.selenium.core.TestBase;
import com.nordstrom.automation.selenium.model.Page;
import com.nordstrom.common.file.PathUtils;

/**
 * This abstract class implements the contract for Selenium Foundation test classes for JUnit.
 */
public abstract class JUnitBase extends TestBase implements ArtifactParams {
    
    /** This method rule manages driver lifetimes and opens initial pages. */
    @Rule(order = 0)
    public final TestWatcher driverTestWatcher = DriverWatcher.getTestWatcher(this);
    
    /** This method rule captures page source on test failures. */
    @Rule(order = 1)
    public final PageSourceCapture pageSourceCapture = new PageSourceCapture(this);
    
    /** This method rule captures screenshots on test failures. */
    @Rule(order = 2)
    public final ScreenshotCapture screenshotCapture = new ScreenshotCapture(this);
    
    private WebDriver driver = null;
    private Page initialPage = null;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<WebDriver> nabDriver() {
        return TestBase.optionalOf(driver);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setDriver(final WebDriver driver) {
        this.driver = driver;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Page> Optional<T> nabInitialPage() {
        return (Optional<T>) TestBase.optionalOf(initialPage);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Page> void setInitialPage(final T pageObj) {
        initialPage = pageObj;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getOutputDirectory() {
        return PathUtils.ReportsDirectory.getPathForObject(this).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTest(final Method method) {
        return null != method.getAnnotation(Test.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBeforeMethod(final Method method) {
        return null != method.getAnnotation(Before.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAfterMethod(final Method method) {
        return null != method.getAnnotation(After.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBeforeClass(final Method method) {
        return null != method.getAnnotation(BeforeClass.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAfterClass(final Method method) {
        return null != method.getAnnotation(AfterClass.class);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>NOTE</b>: This method throws an {@link AssumptionViolatedException} with the specified message.
     */
    public void skipTest(final String message) throws RuntimeException {
        throw new AssumptionViolatedException(message);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public AtomIdentity getAtomIdentity() {
        return screenshotCapture.getAtomIdentity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Description getDescription() {
        return screenshotCapture.getDescription();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Map<String, Object>> getParameters() {
        return Optional.empty();
    }
}
