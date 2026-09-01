package com.nordstrom.automation.selenium.jupiter;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

import com.nordstrom.automation.selenium.core.DriverManager;

/**
 * This class implements a JUnit Platform test execution listener that manages the local Selenium Grid
 * lifecycle - the Jupiter-native equivalent of the JUnit 4 {@code GridWatcher}/{@code DriverListener}
 * pair.
 * <p>
 * Unlike {@code GridWatcher}, no root-runner detection is needed here - {@code testPlanExecutionStarted}/
 * {@code Finished} are guaranteed by the JUnit Platform Launcher to each fire exactly once per Launcher
 * session, regardless of how many test classes or engines are involved.
 * <p>
 * Registered via {@code META-INF/services/org.junit.platform.launcher.TestExecutionListener}. Unlike
 * {@code Extension} auto-detection (see {@code ArgumentsCaptor}'s own javadoc in Jupiter Foundation for
 * that history), {@code TestExecutionListener} SPI registration is ON by default in JUnit Platform -
 * confirmed directly from {@code LauncherFactory}'s own javadoc - no {@code junit-platform.properties}
 * flag required for this one.
 */
public class JupiterGridListener implements TestExecutionListener {

    /**
     * {@inheritDoc}
     * <p>
     * Triggers Grid/sidecar startup via {@link DriverManager#onStart()}.
     */
    @Override
    public void testPlanExecutionStarted(final TestPlan testPlan) {
        DriverManager.onStart();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Triggers Grid/sidecar shutdown via {@link DriverManager#onFinish()}.
     */
    @Override
    public void testPlanExecutionFinished(final TestPlan testPlan) {
        DriverManager.onFinish();
    }
}
