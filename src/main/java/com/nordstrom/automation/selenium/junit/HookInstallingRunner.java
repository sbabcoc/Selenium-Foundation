package com.nordstrom.automation.selenium.junit;

import static net.bytebuddy.matcher.ElementMatchers.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import com.nordstrom.common.base.UncheckedThrow;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;

/**
 * This JUnit test runner uses bytecode enhancement to install hooks on test and configuration methods to enable
 * method pre-processing and post-processing. This closely resembles the {@code IInvokedMethodListener} feature
 * of TestNG. Classes that implement the {@link JUnitMethodWatcher} interface are attached to these hooks via the
 * {@link JUnitMethodWatchers} annotation, which is applied to applicable test classes.
 */
public final class HookInstallingRunner extends BlockJUnit4ClassRunner {
    
    public HookInstallingRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object createTest() throws Exception {
        Object obj = super.createTest();
        if (obj instanceof JUnitBase) {
            obj = installHooks((JUnitBase) obj);
        }
        return obj;
    }
    
    /**
     * Create an enhanced instance of the specified container.
     * 
     * @param <C> container type
     * @param testObj container object to be enhanced
     * @return enhanced container object
     */
    @SuppressWarnings("unchecked")
    public <C extends JUnitBase> C installHooks(C testObj) {
        if (testObj instanceof Hooked) {
            return testObj;
        }
        
        Class<?> testClass = testObj.getClass();
        
        try {
            
            Class<C> proxyType = (Class<C>) new ByteBuddy()
                    .subclass(testClass)
                    .method(isAnnotatedWith(anyOf(Test.class, Before.class,
                                    After.class, BeforeClass.class, AfterClass.class)))
                    .intercept(MethodDelegation.to(JUnitMethodInterceptor.INSTANCE))
                    .implement(Hooked.class)
                    .make()
                    .load(testClass.getClassLoader())
                    .getLoaded();
            
            JUnitMethodInterceptor.INSTANCE.attachWatchers(testClass);
            return proxyType.newInstance();
            
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InstantiationException e) {
            throw UncheckedThrow.throwUnchecked(e);
        }
    }
    
}
