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
    
    private static final String PLATFORM = "Platform";
    
    private final Class<P> platformClass;
    private final Method values;

    public TestNgPlatformBase(Class<P> platformClass) {
        this.platformClass = platformClass;
        try {
            values = platformClass.getMethod("values");
        } catch (NoSuchMethodException | SecurityException e) {
            throw UncheckedThrow.throwUnchecked(e);
        }
    }

    @Override
    public String[] getSubPath() {
        return new String[] { getTargetPlatform().getName() };
    }

    @Override
    @SuppressWarnings("unchecked")
    public P getTargetPlatform() {
        ITestResult testResult = Reporter.getCurrentTestResult();
        if (testResult != null) {
            return (P) testResult.getAttribute(PLATFORM);
        }
        return null;
    }
    
    @Override
    @SuppressWarnings("serial")
    public void activatePlatform(WebDriver driver, P platform) throws PlatformActivationFailedException {
        ITestResult testResult = Reporter.getCurrentTestResult();
        if (testResult != null) {
            String description = testResult.getMethod().getDescription();
            PlatformIdentity<P> identity = DataUtils.fromString(description, new TypeToken<PlatformIdentity<P>>(){}.getType());
            if (identity != null) {
                testResult.setAttribute(PLATFORM, identity.deserialize());
            }
        }
    }

    @Override
    public P[] getValidPlatforms() {
        return values();
    }

    @Override
    public P platformFromString(String name) {
        for (P platform : values()) {
            if (platform.getName().equals(name)) {
                return platform;
            }
        }
        return null;
    }

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
