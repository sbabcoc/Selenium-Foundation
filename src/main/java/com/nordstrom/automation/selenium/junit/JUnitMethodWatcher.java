package com.nordstrom.automation.selenium.junit;

import java.lang.reflect.Method;

/**
 * This interface defines the methods implemented by JUnit method watchers. These watcher are attached to test classes
 * via the {@link JUnitMethodWatchers} annotation. To activate this feature, run with the {@link HookInstallingRunner}.
 */
public interface JUnitMethodWatcher {

    /**
     * Invoked before each test or configuration method is invoked by TestNG
     * 
     * @param obj "enhanced" object upon which the method was invoked
     * @param method {@link Method} object for the invoked method
     * @param args method invocation arguments
     */
    void beforeInvocation(Object obj, Method method, Object[] args);

    /**
     * Invoked after each test or configuration method is invoked by TestNG
     * 
     * @param obj "enhanced" object upon which the method was invoked
     * @param method {@link Method} object for the invoked method
     * @param args method invocation arguments
     */
    void afterInvocation(Object obj, Method method, Object[] args);
}
