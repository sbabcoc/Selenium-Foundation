package com.nordstrom.automation.selenium.jupiter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.exceptions.PlatformActivationFailedException;
import com.nordstrom.automation.selenium.platform.PlatformEnum;
import com.nordstrom.automation.selenium.platform.PlatformTargetable;
import com.nordstrom.common.base.UncheckedThrow;

/**
 * This abstract class implements the contract for Jupiter Selenium Foundation test classes that provide
 * support for the {@link com.nordstrom.automation.selenium.platform.TargetPlatform TargetPlatform}
 * feature - the Jupiter-native equivalent of {@code JUnitPlatformBase}.
 * <p>
 * <b>NOTE ON HISTORY</b>: This originally resolved the target platform via a registered
 * {@code ExecutionCondition} ({@code JupiterTargetPlatformCondition}), stashing the result in an
 * {@code ExtensionContext.Store} for later retrieval. That design relied on an assumption -
 * {@code ExtensionContext.getTestInstance()} being populated when a method-level
 * {@code ExecutionCondition} evaluates under {@code PER_METHOD} - that real test execution confirmed
 * false (a {@code NullPointerException} from {@code getTargetPlatform()} returning {@code null} was the
 * direct evidence). {@code TargetPlatformHandler.resolveTargetPlatform(...)} genuinely needs a live
 * instance for both of its branches (default platform or annotation-driven), so there was no way to make
 * that timing work. Resolution now happens in {@link DriverWatcher} instead, which already receives a
 * guaranteed-live instance via {@code invocationContext.getTarget()} - and since a fresh instance exists
 * per test under {@code PER_METHOD}, the resolved platform is now just a plain field here, matching
 * exactly how {@code JupiterBase}'s own driver/page storage already works safely.
 *
 * @param <P> platform specifier
 */
public abstract class JupiterPlatformBase<P extends Enum<?> & PlatformEnum> extends JupiterBase
        implements PlatformTargetable<P> {

    private final Class<P> platformClass;
    private final Method values;
    private P resolvedPlatform;

    /**
     * Constructor for test classes that provide target platform support.
     *
     * @param platformClass platform specifier
     */
    public JupiterPlatformBase(final Class<P> platformClass) {
        this.platformClass = platformClass;
        try {
            values = platformClass.getMethod("values");
        } catch (NoSuchMethodException | SecurityException e) {
            throw UncheckedThrow.throwUnchecked(e);
        }
    }

    /**
     * Set the target platform resolved for the current test invocation.
     * <p>
     * Package-private: only {@link DriverWatcher} should ever call this, exactly matching the
     * visibility pattern already used for {@code setExtensionContext}/{@code clearExtensionContext} in
     * {@link JupiterTestBase}.
     *
     * @param platform resolved platform; may be {@code null}
     */
    void setResolvedPlatform(final P platform) {
        this.resolvedPlatform = platform;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getSubPath() {
        P platform = getTargetPlatform();
        return (platform != null) ? new String[] { platform.getName() } : new String[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public P getTargetPlatform() {
        return resolvedPlatform;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activatePlatform(final WebDriver driver) {
        P platform = getTargetPlatform();
        if (platform != null) {
            activatePlatform(driver, platform);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activatePlatform(final WebDriver driver, final P platform) throws PlatformActivationFailedException {
        // by default, do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public P[] getValidPlatforms() {
        return values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public P platformFromString(final String name) {
        for (P platform : values()) {
            if (platform.getName().equals(name)) {
                return platform;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<P> getPlatformType() {
        return platformClass;
    }

    private P[] values() {
        return invoke(values);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(final Method method, final Object... parameters) {
        try {
            return (T) method.invoke(null, parameters);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw UncheckedThrow.throwUnchecked(e);
        }
    }
}
