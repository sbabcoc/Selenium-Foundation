package com.nordstrom.automation.selenium.jupiter;

import java.util.Optional;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.model.Page;

/**
 * Base class for Selenium Foundation Jupiter tests using the {@code PER_CLASS} test-instance lifecycle
 * — e.g. for {@code @Nested} test classes, or non-static {@code @BeforeAll}/{@code @AfterAll}.
 * <p>
 * <b>NOTE</b>: {@code @Execution(CONCURRENT)} is supported here — the same shared instance may have
 * multiple invocations in flight simultaneously on different threads. All per-invocation state
 * (context, driver, initial page) is therefore stored via {@link ThreadLocal} rather than plain fields:
 * a single invocation's entire before/test/after chain always runs on one thread even when sibling
 * invocations run concurrently, so keying by thread correctly isolates each invocation without any of
 * them ever colliding. Driver/page storage itself goes a step further and uses the current invocation's
 * own {@link ExtensionContext.Store} rather than the {@code ThreadLocal} directly, since a
 * {@code Store} entry is inherently scoped to one invocation regardless of thread reuse.
 */
@TestInstance(Lifecycle.PER_CLASS)
public abstract class JupiterClassBase extends JupiterTestBase {

    private static final Namespace NAMESPACE = Namespace.create(JupiterClassBase.class);
    private static final String DRIVER_KEY = "driver";
    private static final String INITIAL_PAGE_KEY = "initialPage";

    /**
     * Shared across all instances, matching the precedent set by TestNG Foundation's
     * {@code ExecutionFlowController.fromBefore}/{@code fromMethod} fields, which solve the identical
     * problem: TestNG's default lifecycle already reuses one test class instance across every method in
     * the class, the same shared-instance shape as {@code PER_CLASS} here. Only one invocation ever runs
     * on a given thread at a time regardless of which instance owns it, so one {@code static} field
     * serves every instance correctly. {@code InheritableThreadLocal} (not plain {@code ThreadLocal}),
     * also matching that precedent, so a value survives if the underlying engine ever spawns a child
     * thread mid-invocation.
     */
    private static final ThreadLocal<ExtensionContext> CURRENT_CONTEXT = new InheritableThreadLocal<>();

    @Override
    public Optional<WebDriver> nabDriver() {
        return Optional.ofNullable((WebDriver) store().get(DRIVER_KEY));
    }

    @Override
    public void setDriver(final WebDriver driver) {
        store().put(DRIVER_KEY, driver);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Page> Optional<T> nabInitialPage() {
        return Optional.ofNullable((T) store().get(INITIAL_PAGE_KEY));
    }

    @Override
    public <T extends Page> void setInitialPage(final T initialPage) {
        store().put(INITIAL_PAGE_KEY, initialPage);
    }

    @Override
    protected ExtensionContext getExtensionContext() {
        return CURRENT_CONTEXT.get();
    }

    @Override
    void setExtensionContext(final ExtensionContext context) {
        CURRENT_CONTEXT.set(context);
    }

    @Override
    void clearExtensionContext() {
        // ThreadLocal.remove(), not just set(null) — the pooled thread will be reused by unrelated
        // future invocations, and a lingering entry (even a null-valued one) is a leak either way
        CURRENT_CONTEXT.remove();
    }

    private Store store() {
        ExtensionContext context = getExtensionContext();
        if (context == null) {
            throw new IllegalStateException(
                    "No extension context is available for this invocation; " +
                    "was this called outside of a wrapped invocation?");
        }
        return context.getStore(NAMESPACE);
    }
}
