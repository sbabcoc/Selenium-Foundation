package com.nordstrom.automation.selenium.junit;

import java.lang.reflect.Method;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import com.nordstrom.automation.selenium.listeners.DriverManager;
import com.nordstrom.automation.selenium.support.TestBase;

public class DriverWatcher implements JUnitMethodWatcher {

    @Override
    public void beforeInvocation(Object obj, Method method, Object[] args) {
        DriverManager.beforeInvocation(obj, method);
    }

    @Override
    public void afterInvocation(Object obj, Method method, Object[] args) {
        DriverManager.afterInvocation(obj, method);
    }

    public static TestWatcher getTestWatcher(final TestBase obj) {
        return new TestWatcher() {
            @Override
            protected void finished(Description description) {
                DriverManager.closeDriver(obj);
            }
        };
    }
}
