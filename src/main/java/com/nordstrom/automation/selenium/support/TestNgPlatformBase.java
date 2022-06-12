package com.nordstrom.automation.selenium.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Listeners;

import com.google.common.reflect.TypeToken;
import com.nordstrom.automation.selenium.exceptions.PlatformActivationFailedException;
import com.nordstrom.automation.selenium.listeners.PlatformInterceptor;
import com.nordstrom.automation.selenium.listeners.PlatformInterceptor.PlatformIdentity;
import com.nordstrom.automation.selenium.platform.PlatformEnum;
import com.nordstrom.automation.selenium.platform.PlatformTargetable;
import com.nordstrom.automation.selenium.utility.DataUtils;
import com.nordstrom.common.base.UncheckedThrow;

@Listeners(PlatformInterceptor.class)
public abstract class TestNgPlatformBase<P extends Enum<?> & PlatformEnum> extends TestNgBase implements PlatformTargetable<P> {
    
    private final Class<P> platformClass;
    private final Method values;

    private static final String PLATFORM = "Platform";
    
    public TestNgPlatformBase(Class<P> platformClass) {
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
    @SuppressWarnings({ "unchecked", "serial" })
    public P getTargetPlatform() {
        P platform = null;
        ITestResult testResult = Reporter.getCurrentTestResult();
        if (testResult != null) {
            platform = (P) testResult.getAttribute(PLATFORM);
            if (platform == null) {
                String description = testResult.getMethod().getDescription();
                PlatformIdentity<P> identity = DataUtils.fromString(description, new TypeToken<PlatformIdentity<P>>(){}.getType());
                if (identity != null) {
                    platform = identity.deserialize();
                    testResult.setAttribute(PLATFORM, platform);
                }                
            }
        }
        return platform;
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
