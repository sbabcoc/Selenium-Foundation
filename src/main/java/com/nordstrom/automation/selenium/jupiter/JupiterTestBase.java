package com.nordstrom.automation.selenium.jupiter;

import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.nordstrom.automation.selenium.core.TestBase;
import com.nordstrom.common.base.StackTrace;
import com.nordstrom.common.file.PathUtils;

/**
 * This abstract class provides the contract members that are common to both {@link TestInstance.Lifecycle}
 * variants of Selenium Foundation's JUnit 5 support: annotation-based method classification, test-skip
 * behavior, output directory resolution, and driver lifecycle management via {@link DriverWatcher}.
 * <p>
 * <b>NOTE</b>: This class deliberately leaves driver/initial-page storage ({@link #nabDriver},
 * {@link #setDriver}, {@link #nabInitialPage}, {@link #setInitialPage}) abstract. The two concrete
 * lifecycle-specific base classes ({@code JupiterBase} for {@code PER_METHOD}, {@code JupiterClassBase}
 * for {@code PER_CLASS}) each supply the storage strategy appropriate to their declared
 * {@code @TestInstance} lifecycle.
 */
public abstract class JupiterTestBase extends TestBase {

    /**
     * Get the {@link ExtensionContext} for the invocation currently in progress against this instance.
     * <p>
     * <b>NOTE</b>: This is deliberately abstract, not a single field on this shared class — the correct
     * storage strategy differs by lifecycle exactly as it does for {@code nabDriver}/{@code setDriver}.
     * {@code JupiterBase} ({@code PER_METHOD}) can use a plain field: a fresh instance per test means no
     * concurrent access is possible against it. {@code JupiterClassBase} ({@code PER_CLASS}) cannot — the
     * same instance may be shared across concurrently-executing invocations if the test author opts into
     * {@code @Execution(CONCURRENT)}, so it must use a {@code ThreadLocal} instead: a single invocation's
     * entire before/test/after chain always runs on one thread, even under concurrent execution of
     * sibling methods, so keying storage by thread correctly isolates one invocation's context from
     * another's without any risk of the two colliding.
     *
     * @return current extension context; {@code null} if no invocation is currently in progress
     */
    protected abstract ExtensionContext getExtensionContext();

    /**
     * Set the {@link ExtensionContext} for the invocation currently in progress against this instance.
     * Package-private: only {@link DriverWatcher} should ever call this.
     *
     * @param context current extension context
     */
    abstract void setExtensionContext(final ExtensionContext context);

    /**
     * Release any resources associated with the context most recently set via
     * {@link #setExtensionContext(ExtensionContext)} for the invocation now ending. {@code JupiterBase}
     * can no-op this. {@code JupiterClassBase} must call {@code ThreadLocal.remove()} here — otherwise,
     * since Jupiter's concurrent executor reuses pooled threads across unrelated later invocations, a
     * stale context could leak onto a completely different invocation that happens to reuse the same
     * physical thread.
     */
    abstract void clearExtensionContext();

    /**
     * This extension manages driver lifetimes and opens initial pages, mirroring the behavior of
     * {@code DriverWatcher} in the JUnit 4 integration. It's declared here (not in the lifecycle-specific
     * subclasses) because its logic is driven entirely through the {@link TestBase} contract and never
     * touches instance storage directly.
     */
    @RegisterExtension
    final DriverWatcher driverWatcher = new DriverWatcher();

    /**
     * Captures page source on test failure. Registered here (not per-subclass) since, like
     * {@code driverWatcher}, its logic — {@link com.nordstrom.automation.jupiter.ArtifactCollector} —
     * needs no lifecycle-specific storage; it reads everything it needs directly from the
     * {@link ExtensionContext} its {@code testFailed} callback receives.
     */
    @RegisterExtension
    public final PageSourceCapture pageSourceCapture = new PageSourceCapture();

    /**
     * Captures a screenshot on test failure. See {@link #pageSourceCapture}.
     */
    @RegisterExtension
    public final ScreenshotCapture screenshotCapture = new ScreenshotCapture();

    /**
     * Provides automatic retry of failed tests via Jupiter Foundation's {@link com.nordstrom.automation.jupiter.RetryExtension},
     * with driver-lifecycle handling re-applied on every retry attempt beyond the first (see
     * {@link SeleniumRetryExtension}'s own javadoc for why that's necessary). Built and demonstrated
     * standalone earlier, but never actually registered here until now - this field is what makes it
     * active for any Selenium Foundation test class extending {@code JupiterBase}/{@code JupiterClassBase}.
     */
    @RegisterExtension
    final SeleniumRetryExtension retryExtension = new SeleniumRetryExtension();

    /**
     * Capture page source on demand, independent of test failure — the Jupiter equivalent of calling
     * JUnit 4's {@code captureArtifact(Throwable)} directly rather than waiting for {@code failed(...)}.
     * <p>
     * Safe to call from anywhere within the current test method's own execution: {@link DriverWatcher}
     * guarantees {@link #getExtensionContext()} is already populated for that entire span, since it's
     * set before {@code invocation.proceed()} — the call that runs the test method body — and only
     * cleared afterward.
     *
     * @param reason impetus for capture request; may be {@code null}, though
     * {@link #capturePageSource()} is preferable when there's no real exception to pass, since a
     * {@code null} reason discards the diagnostic value a captured stack trace would otherwise add
     * @return (optional) path at which the captured artifact was stored
     */
    protected java.util.Optional<java.nio.file.Path> capturePageSource(final Throwable reason) {
        return pageSourceCapture.captureArtifact(getExtensionContext(), reason);
    }

    /**
     * Capture page source on demand, using a captured stack trace of the calling thread as the reason.
     * Prefer this over {@link #capturePageSource(Throwable)} with an explicit {@code null} — per
     * JUnit-Foundation's own {@code ArtifactCollectorOnDemand.testOnDemandCapture()} example, {@code null}
     * is valid, but {@link StackTrace#here()} costs nothing and gives the eventual log line
     * ("Saving captured artifact to (...)") real diagnostic content about where the call originated,
     * rather than none at all.
     *
     * @return (optional) path at which the captured artifact was stored
     */
    protected java.util.Optional<java.nio.file.Path> capturePageSource() {
        return capturePageSource(com.nordstrom.common.base.StackTrace.here());
    }

    /**
     * Capture a screenshot on demand, independent of test failure. See {@link #capturePageSource(Throwable)}.
     *
     * @param reason impetus for capture request; may be {@code null} — see
     * {@link #capturePageSource(Throwable)} for why {@link #captureScreenshot()} is usually preferable
     * @return (optional) path at which the captured artifact was stored
     */
    protected java.util.Optional<java.nio.file.Path> captureScreenshot(final Throwable reason) {
        return screenshotCapture.captureArtifact(getExtensionContext(), reason);
    }

    /**
     * Capture a screenshot on demand, using a captured stack trace of the calling thread as the reason.
     * See {@link #capturePageSource()}.
     *
     * @return (optional) path at which the captured artifact was stored
     */
    protected java.util.Optional<java.nio.file.Path> captureScreenshot() {
        return captureScreenshot(com.nordstrom.common.base.StackTrace.here());
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
        return null != method.getAnnotation(BeforeEach.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAfterMethod(final Method method) {
        return null != method.getAnnotation(AfterEach.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBeforeClass(final Method method) {
        return null != method.getAnnotation(BeforeAll.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAfterClass(final Method method) {
        return null != method.getAnnotation(AfterAll.class);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>NOTE</b>: This method aborts the test via {@link Assumptions#abort(String)}, which JUnit 5
     * reports as "aborted" (its analog of JUnit 4's {@code AssumptionViolatedException}-driven skip
     * and TestNG's {@code SkipException}).
     */
    @Override
    public void skipTest(final String message) throws RuntimeException {
        Assumptions.abort(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOutputDirectory() {
        return PathUtils.ReportsDirectory.getPathForObject(this).toString();
    }
}
