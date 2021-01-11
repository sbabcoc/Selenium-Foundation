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

public abstract class JUnitPlatformBase<P extends Enum<?> & PlatformEnum> extends JUnitBase implements PlatformTargetable<P> {
    
    private final Class<P> platformClass;
    private final Method values;

    @Rule
    public TargetPlatformRule<?> targetPlatformRule = new TargetPlatformRule<>(this);

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
        return new String[] { getTargetPlatform().getName() };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public P getTargetPlatform() {
        return (P) targetPlatformRule.getPlatform();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void activatePlatform(WebDriver driver) {
        P platform = (P) targetPlatformRule.getPlatform();
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
