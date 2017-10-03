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
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.model.Page;
import com.nordstrom.automation.selenium.support.TestBase;

public abstract class JUnitBase implements TestBase {
    
    @ClassRule
    public static final ExternalResource resource = UnitTestWatcher.getClassWatcher();
    
    @Rule
    public final TestWatcher watcher = UnitTestWatcher.getTestWatcher(this);
    
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
}
