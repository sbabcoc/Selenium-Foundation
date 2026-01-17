package com.nordstrom.automation.selenium.core;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.nordstrom.automation.selenium.exceptions.JavaScriptStackTrace;
import com.nordstrom.automation.selenium.exceptions.ScriptExecutionException;

/**
 * Factory that creates Java exceptions based on requested class names from JS.
 * Supports a baseline whitelist for Selenium exceptions, AssertionError, RuntimeExceptions,
 * and optionally configured additional classes or packages.
 */
public final class ExceptionFactory {

    private final Set<String> whitelist;

    /**
     * Construct a factory with optional additional whitelist entries.
     * Entries may be:
     *   - Fully qualified class names, e.g., "com.foo.BarException"
     *   - Package wildcard "*", e.g., "com.foo.*" (package only)
     *   - Package subtree wildcard "**", e.g., "org.openqa.selenium.**" (all subpackages)
     *
     * Baseline whitelist always includes:
     *   - org.openqa.selenium.**
     *   - AssertionError (special-cased)
     *   - RuntimeException (special-cased)
     */
    public ExceptionFactory(Set<String> additionalWhitelist) {
        Set<String> set = new HashSet<>();
        // Always allow all Selenium exceptions
        set.add("org.openqa.selenium.**");
        // Add user-specified entries if present
        if (additionalWhitelist != null) {
            set.addAll(additionalWhitelist);
        }
        this.whitelist = Collections.unmodifiableSet(set);
    }

    /**
     * Create the requested exception if allowed.
     */
    @SuppressWarnings("unchecked")
    public Throwable create(String className, String message, String jsStack) {
        Objects.requireNonNull(className, "className");

        try {
            Class<?> clazz = Class.forName(className);

            // Always allow AssertionError
            if (clazz == AssertionError.class) {
                AssertionError ae = new AssertionError(message);
                attachStack(ae, jsStack);
                return ae;
            }

            // Always allow RuntimeException (including subclasses)
            if (RuntimeException.class.isAssignableFrom(clazz)) {
                Class<? extends RuntimeException> exClass =
                        (Class<? extends RuntimeException>) clazz;

                RuntimeException ex = instantiate(exClass, message);
                attachStack(ex, jsStack);
                return ex;
            }

            // All other exceptions must be explicitly allowed by the whitelist
            if (!isAllowed(clazz)) {
                throw ScriptExecutionException.policyViolation(
                        className,
                        "Requested exception not allowed by configuration: " + className
                );
            }

            // If somehow allowed by whitelist, attempt to instantiate
            return instantiate((Class<? extends RuntimeException>) clazz, message);

        } catch (ClassNotFoundException e) {
            throw ScriptExecutionException.instantiationFailure(
                    className,
                    "Requested exception class not found: " + className,
                    e
            );
        } catch (ReflectiveOperationException e) {
            throw ScriptExecutionException.instantiationFailure(
                    className,
                    "Failed to instantiate requested exception: " + className,
                    e
            );
        }
    }

    /* ==============================
       Whitelist / policy
       ============================== */

    private boolean isAllowed(Class<?> clazz) {
        String name = clazz.getName();

        for (String rule : whitelist) {
            if (rule.endsWith(".**")) {
                // subtree wildcard
                String prefix = rule.substring(0, rule.length() - 3);
                if (name.startsWith(prefix)) return true;
            } else if (rule.endsWith(".*")) {
                // package-only wildcard
                String prefix = rule.substring(0, rule.length() - 2);
                int lastDot = name.lastIndexOf('.');
                String packageName = lastDot > 0 ? name.substring(0, lastDot) : "";
                if (packageName.equals(prefix)) return true;
            } else {
                // exact class match
                if (name.equals(rule)) return true;
            }
        }
        return false;
    }

    /* ==============================
       Instantiation / stack
       ============================== */

    private static RuntimeException instantiate(
            Class<? extends RuntimeException> exClass,
            String message) throws ReflectiveOperationException {

        try {
            Constructor<? extends RuntimeException> ctor =
                exClass.getConstructor(String.class);
            return ctor.newInstance(message);
        } catch (NoSuchMethodException ignored) {
            return exClass.getConstructor().newInstance();
        }
    }

    private static void attachStack(Throwable t, String jsStack) {
        if (jsStack != null && !jsStack.isEmpty()) {
            t.addSuppressed(new JavaScriptStackTrace(jsStack));
        }
    }
}
