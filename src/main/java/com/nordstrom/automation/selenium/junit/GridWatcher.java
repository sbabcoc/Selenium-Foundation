package com.nordstrom.automation.selenium.junit;

import com.nordstrom.automation.junit.LifecycleHooks;
import com.nordstrom.automation.junit.RunnerWatcher;
import com.nordstrom.automation.selenium.core.DriverManager;

/**
 * This class implements a JUnit runner watcher that manages the local Selenium Grid lifecycle.
 * <p>
 * When the root runner starts, this watcher triggers Grid startup via
 * {@link DriverManager#onStart()}. Grid shutdown is handled separately by
 * {@link DriverListener} via the {@link com.nordstrom.automation.junit.ShutdownListener}
 * mechanism.
 * <p>
 * This watcher is registered via the {@code META-INF/services} ServiceLoader mechanism
 * and is activated automatically when JUnit Foundation is on the classpath.
 */
public class GridWatcher implements RunnerWatcher {

    /**
     * {@inheritDoc}
     * <p>
     * <b>NOTE</b>: Grid startup is only triggered for the root runner — identified
     * by having no parent — to ensure the Grid is launched exactly once per test run.
     */
    @Override
    public void runStarted(Object runner) {
        // only trigger on root runner - root has no parent
        if (LifecycleHooks.getParentOf(runner) == null) {
            DriverManager.onStart();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>NOTE</b>: Grid shutdown is handled by {@link DriverListener} via the
     * {@link com.nordstrom.automation.junit.ShutdownListener} mechanism.
     */
    @Override
    public void runFinished(Object runner) {
        // shutdown handled by DriverListener via ShutdownListener
    }
}
