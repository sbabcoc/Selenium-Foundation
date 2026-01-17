package com.nordstrom.automation.selenium.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import com.nordstrom.automation.selenium.interfaces.ExceptionPolicy;

public final class ConfigurableExceptionPolicy implements ExceptionPolicy {

    private static final Set<Class<?>> BASELINE;

    static {
        Set<Class<?>> base = new HashSet<Class<?>>();
        base.add(RuntimeException.class);
        base.add(IllegalArgumentException.class);
        base.add(IllegalStateException.class);
        base.add(AssertionError.class);
        base.add(TimeoutException.class);
        BASELINE = Collections.unmodifiableSet(base);
    }

    private final Set<Class<?>> allowed;

    public ConfigurableExceptionPolicy(Collection<String> additionalClassNames) {
        Set<Class<?>> set = new HashSet<Class<?>>(BASELINE);

        if (additionalClassNames != null) {
            for (String name : additionalClassNames) {
                set.add(resolve(name));
            }
        }

        this.allowed = Collections.unmodifiableSet(set);
    }

    @Override
    public boolean isAllowed(Class<?> type) {
        for (Class<?> allowedType : allowed) {
            if (allowedType.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    private static Class<?> resolve(String className) {
        try {
            Class<?> cls = Class.forName(className);

            if (!Throwable.class.isAssignableFrom(cls)) {
                throw new IllegalArgumentException(className + " is not a Throwable");
            }

            if (Error.class.isAssignableFrom(cls)) {
                throw new IllegalArgumentException(className + " is an Error and not allowed");
            }

            return cls;

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Configured exception class not found: " + className, e);
        }
    }
}
