package com.nordstrom.automation.selenium.junit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Rule;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.exceptions.PlatformActivationFailedException;
import com.nordstrom.automation.selenium.platform.PlatformEnum;
import com.nordstrom.automation.selenium.platform.PlatformTargetable;
import com.nordstrom.automation.selenium.platform.TargetPlatformRule;
import com.nordstrom.common.base.UncheckedThrow;

/**
 * This abstract class implements the contract for JUnit Selenium Foundation test classes that provide support
 * for the {@link com.nordstrom.automation.selenium.platform.TargetPlatform TargetPlatform} feature.
 * 
 * @param <P> platform specifier
 */
public abstract class JUnitPlatformBase<P extends Enum<?> & PlatformEnum> extends JUnitBase implements PlatformTargetable<P> {
    
    private final Class<P> platformClass;
    private final Method values;

    /** This method rule implements the target platform feature */
    @Rule
    public TargetPlatformRule<P> targetPlatformRule = new TargetPlatformRule<>(this);

    /**
     * Constructor for test classes that provide target platform support.
     * 
     * @param platformClass platform specifier
     */
    public JUnitPlatformBase(Class<P> platformClass) {
        this.platformClass = platformClass;
        try {
            values = platformClass.getMethod("values");
        } catch (NoSuchMethodException | SecurityException e) {
            throw UncheckedThrow.throwUnchecked(e);
        }
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
        return targetPlatformRule.getPlatform();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activatePlatform(WebDriver driver) {
        P platform = getTargetPlatform();
        if (platform != null) {
            activatePlatform(driver, platform);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void activatePlatform(WebDriver driver, P platform) throws PlatformActivationFailedException {
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
    public P platformFromString(String name) {
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
    private static <T> T invoke(Method method, Object... parameters) {
        try {
            return (T) method.invoke(null, parameters);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw UncheckedThrow.throwUnchecked(e);
        }
    }

}
