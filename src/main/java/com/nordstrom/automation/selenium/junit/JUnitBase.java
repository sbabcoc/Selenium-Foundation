package com.nordstrom.automation.selenium.junit;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.junit.ArtifactParams;
import com.nordstrom.automation.junit.HookInstallingRunner;
import com.nordstrom.automation.junit.MethodWatchers;
import com.nordstrom.automation.selenium.core.TestBase;
import com.nordstrom.automation.selenium.model.Page;

/**
 * This abstract class implements the contract for Selenium Foundation test classes for JUnit.
 */
@RunWith(HookInstallingRunner.class)
@MethodWatchers({DriverWatcher.class})
public abstract class JUnitBase implements TestBase, ArtifactParams {
    
    /** This class rule performs end-of-class clean-up of drivers and local Grid. */
    @ClassRule
    public static final ExternalResource resource = DriverWatcher.getClassWatcher();
    
    /** This method rule manages driver lifetimes and opens initial pages. */
    @Rule
    public final TestWatcher driverWatcher = DriverWatcher.getTestWatcher(this);
    
    @Rule
    public final ScreenshotCapture screenshotCapture = new ScreenshotCapture(this);
    
    private Optional<WebDriver> optDriver = Optional.empty();
    private Optional<Page> optInitialPage = Optional.empty();

    @Override
    public Optional<WebDriver> nabDriver() {
        return optDriver;
    }

    @Override
    public Optional<WebDriver> setDriver(WebDriver driver) {
        optDriver = TestBase.optionalOf(driver);
        return optDriver;
    }

    @Override
    public Optional<Page> nabInitialPage() {
        return optInitialPage;
    }

    @Override
    public Optional<Page> setInitialPage(Page pageObj) {
        optInitialPage = TestBase.optionalOf(pageObj);
        return optInitialPage;
    }

    @Override
    public String getOutputDirectory() {
        return TestBase.getOutputDir();
    }
    
    @Override
    public boolean isTest(Method method) {
        return null != method.getAnnotation(Test.class);
    }

    @Override
    public boolean isBeforeMethod(Method method) {
        return null != method.getAnnotation(Before.class);
    }

    @Override
    public boolean isAfterMethod(Method method) {
        return null != method.getAnnotation(After.class);
    }

    @Override
    public boolean isBeforeClass(Method method) {
        return null != method.getAnnotation(BeforeClass.class);
    }

    @Override
    public boolean isAfterClass(Method method) {
        return null != method.getAnnotation(AfterClass.class);
    }
    
    @Override
    public Description getDescription() {
        return screenshotCapture.getDescription();
    }
}
