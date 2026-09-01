package com.nordstrom.automation.selenium.jupiter;

import java.lang.reflect.Method;

import com.nordstrom.automation.jupiter.RetryExtension;
import com.nordstrom.automation.selenium.core.DriverManager;

/**
 * Selenium Foundation's own retry extension, extending Jupiter Foundation's generic
 * {@link RetryExtension} exactly per the pattern its own javadoc documents: override the per-attempt
 * hooks to run framework-specific behavior around every retry, since attempts beyond the first bypass
 * the normal {@code InvocationInterceptor} chain (and therefore bypass {@code DriverWatcher} too).
 * <p>
 * <b>NOTE</b>: This class does NOT override {@code getMaxRetry(...)}. The retry count is a Jupiter
 * Foundation setting ({@code JupiterConfig.JupiterSettings.MAX_RETRY}), not a Selenium Foundation
 * concern - exactly matching how {@code TestNGSettings.MAX_RETRY} belongs to TestNG Foundation, never
 * redefined by Selenium Foundation's own {@code TestNgBase}/{@code TestNgPlatformBase}.
 * <p>
 * Registered in place of the base class - not alongside it - on {@code JupiterTestBase}, matching how
 * TestNG Foundation's own {@code RetryManager} extension pattern works: one active implementation, not
 * simultaneous base-and-subclass activation.
 */
public class SeleniumRetryExtension extends RetryExtension {

    /**
     * {@inheritDoc}
     * <p>
     * Runs the same {@link DriverManager#beforeInvocation(Object, Method)} call {@code DriverWatcher}
     * would have made had this attempt gone through the normal interceptor chain.
     */
    @Override
    protected void beforeAttempt(final Object instance, final Method method) {
        DriverManager.beforeInvocation(instance, method);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Runs the matching {@link DriverManager#afterInvocation(Object, Method)} call after each attempt,
     * regardless of whether it passed or failed - mirroring {@code DriverWatcher}'s own
     * {@code finally}-block behavior.
     */
    @Override
    protected void afterAttempt(final Object instance, final Method method, final Throwable thrown) {
        DriverManager.afterInvocation(instance, method);
    }
}
