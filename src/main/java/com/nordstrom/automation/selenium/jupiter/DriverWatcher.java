package com.nordstrom.automation.selenium.jupiter;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import com.nordstrom.automation.selenium.core.DriverManager;
import com.nordstrom.automation.selenium.core.TestBase;
import com.nordstrom.automation.selenium.platform.PlatformEnum;
import com.nordstrom.automation.selenium.platform.PlatformTargetable;
import com.nordstrom.automation.selenium.platform.TargetPlatform;
import com.nordstrom.automation.selenium.platform.TargetPlatformHandler;
import com.nordstrom.automation.selenium.SeleniumConfig;

/**
 * This extension performs the same basic functions as the JUnit 4 {@code DriverWatcher}: managing
 * Selenium driver lifetime, local Grid startup coordination, and initial-page dispensing, driven
 * entirely through {@link DriverManager}'s framework-agnostic static methods against the
 * {@link com.nordstrom.automation.selenium.core.TestBase} contract.
 * <p>
 * Unlike JUnit 4's single {@code MethodWatcher} callback pair, Jupiter's {@link InvocationInterceptor}
 * requires a distinct typed override per method category, and each override is responsible for
 * invoking {@code invocation.proceed()} itself.
 * <p>
 * <b>NOTE</b>: Under the default {@code PER_METHOD} test-instance lifecycle, {@code @BeforeAll}/
 * {@code @AfterAll} methods are required to be {@code static}, so
 * {@link ReflectiveInvocationContext#getTarget()} returns an empty {@link Optional} for them — there is
 * no live instance to associate driver state with. This watcher no-ops {@link DriverManager} calls in
 * that case; class-level driver acquisition via {@code @BeforeAll}/{@code @AfterAll} is only meaningful
 * under the {@code PER_CLASS} lifecycle, where a real target instance is always present.
 */
final class DriverWatcher implements InvocationInterceptor, AfterEachCallback {

    @Override
    public void interceptBeforeAllMethod(final Invocation<Void> invocation,
            final ReflectiveInvocationContext<Method> invocationContext,
            final ExtensionContext extensionContext) throws Throwable {
        wrapInvocation(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptBeforeEachMethod(final Invocation<Void> invocation,
            final ReflectiveInvocationContext<Method> invocationContext,
            final ExtensionContext extensionContext) throws Throwable {
        wrapInvocation(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestMethod(final Invocation<Void> invocation,
            final ReflectiveInvocationContext<Method> invocationContext,
            final ExtensionContext extensionContext) throws Throwable {
        wrapInvocation(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptAfterEachMethod(final Invocation<Void> invocation,
            final ReflectiveInvocationContext<Method> invocationContext,
            final ExtensionContext extensionContext) throws Throwable {
        wrapInvocation(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptAfterAllMethod(final Invocation<Void> invocation,
            final ReflectiveInvocationContext<Method> invocationContext,
            final ExtensionContext extensionContext) throws Throwable {
        wrapInvocation(invocation, invocationContext, extensionContext);
    }

    /**
     * Run {@link DriverManager}'s before/after-invocation processing around the wrapped invocation,
     * mirroring the before/after pairing that JUnit 4's {@code MethodWatcher} provided implicitly.
     *
     * @param invocation the intercepted invocation; must be explicitly proceeded or skipped
     * @param invocationContext supplies the target instance (absent for static methods) and the method
     * @param extensionContext written onto the target's {@code currentContext} field so instance
     * methods with no context parameter of their own (e.g. {@code activatePlatform(WebDriver)}) can
     * still reach it
     * @throws Throwable whatever the wrapped invocation itself throws
     */
    private void wrapInvocation(final Invocation<Void> invocation,
            final ReflectiveInvocationContext<Method> invocationContext,
            final ExtensionContext extensionContext) throws Throwable {
        Optional<Object> target = invocationContext.getTarget();
        Method method = invocationContext.getExecutable();

        try {
            if (target.isPresent()) {
                Object instance = target.get();
                if (instance instanceof JupiterTestBase) {
                    ((JupiterTestBase) instance).setExtensionContext(extensionContext);
                }

                if ((instance instanceof TestBase) && ((TestBase) instance).isTest(method)
                        && (instance instanceof PlatformTargetable)) {
                    checkTargetPlatform((TestBase) instance, (PlatformTargetable) instance, method);
                }

                DriverManager.beforeInvocation(instance, method);
            }
            invocation.proceed();
        } finally {
            if (target.isPresent()) {
                DriverManager.afterInvocation(target.get(), method);
                if (target.get() instanceof JupiterTestBase) {
                    ((JupiterTestBase) target.get()).clearExtensionContext();
                }
            }
        }
    }

    /**
     * Resolve and check the target platform for the current test method invocation, using the
     * guaranteed-live instance {@link DriverWatcher} already holds - see {@link JupiterPlatformBase}'s
     * javadoc for why this can't be done via a registered {@code ExecutionCondition} instead.
     *
     * @param testBase test instance, used to call {@code skipTest(...)} if the platform doesn't match
     * @param platformTargetable same instance, viewed through the {@code PlatformTargetable} contract
     * @param method invoked test method
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void checkTargetPlatform(final TestBase testBase, final PlatformTargetable platformTargetable,
            final Method method) {
        String contextPlatform = SeleniumConfig.getConfig().getContextPlatform();
        TargetPlatform annotation = method.getAnnotation(TargetPlatform.class);

        // kept as Object, not cast down to PlatformEnum yet - JupiterPlatformBase<P extends Enum<?> &
        // PlatformEnum> has TWO bounds on P, and raw-type erasure always uses the FIRST one (Enum, not
        // PlatformEnum) - casting straight to PlatformEnum here would lose the half of the type
        // information the raw setResolvedPlatform(...) call below actually needs
        Object resolved = TargetPlatformHandler.resolveTargetPlatform(platformTargetable, annotation);
        PlatformEnum platform = (PlatformEnum) resolved;

        if (platformTargetable instanceof JupiterPlatformBase) {
            ((JupiterPlatformBase) platformTargetable).setResolvedPlatform((Enum<?>) resolved);
        }

        if (!TargetPlatformHandler.shouldRun(contextPlatform, platform)) {
            testBase.skipTest(String.format("%s.%s() doesn't specify platform '%s'",
                    method.getDeclaringClass().getName(), method.getName(), contextPlatform));
        }
    }

        /**
     * {@inheritDoc}
     * <p>
     * This is the safety-net equivalent of the JUnit 4 {@code TestWatcher.finished()} override in
     * {@code DriverWatcher.getTestWatcher()} — it fires unconditionally once per test method, regardless
     * of outcome, ensuring the driver is closed even if something upstream of {@code @AfterEach}
     * processing didn't already do so.
     */
    @Override
    public void afterEach(final ExtensionContext context) {
        Optional<Object> instance = context.getTestInstance();
        if (instance.isPresent()) {
            DriverManager.closeDriver(instance.get());
        }
    }
}
