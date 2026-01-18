package com.nordstrom.automation.selenium.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import com.nordstrom.automation.selenium.interfaces.ExceptionPolicy;

/**
 * {@code ConfigurableExceptionPolicy} defines which Java exception types are
 * permitted to be instantiated and propagated based on information returned
 * from JavaScript execution.
 *
 * <p>This policy is used as a safety boundary when converting JavaScript-side
 * failures into Java {@link Throwable} instances. Only exception types explicitly
 * allowed by this policy may be constructed; all others are rejected as
 * configuration or policy violations.</p>
 *
 * <h2>Baseline allowed exceptions</h2>
 * <p>A fixed baseline of commonly used, non-fatal exception types is always
 * permitted. These represent standard programming and runtime failures that
 * are safe to propagate:</p>
 *
 * <ul>
 *   <li>{@link RuntimeException}</li>
 *   <li>{@link IllegalArgumentException}</li>
 *   <li>{@link IllegalStateException}</li>
 *   <li>{@link AssertionError}</li>
 *   <li>{@link TimeoutException}</li>
 * </ul>
 *
 * <p>The baseline set is immutable and cannot be removed or overridden.</p>
 *
 * <h2>Configuration-based extension</h2>
 * <p>Additional allowed exception types may be supplied via configuration as
 * fully-qualified class names. These are resolved at construction time and
 * added to the baseline set.</p>
 *
 * <p>Configured classes must:</p>
 * <ul>
 *   <li>Exist on the classpath</li>
 *   <li>Extend {@link Throwable}</li>
 *   <li><strong>Not</strong> extend {@link Error}</li>
 * </ul>
 *
 * <p>Any violation of these constraints results in an
 * {@link IllegalArgumentException} at configuration time.</p>
 *
 * <h2>Type matching semantics</h2>
 * <p>An exception is considered allowed if its concrete type is assignable
 * to any allowed class. In other words, subclasses of allowed types are
 * implicitly permitted.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * // Allowed if RuntimeException is in the policy
 * IllegalStateException instanceof RuntimeException  -> allowed
 * }</pre>
 *
 * <p>This class is immutable and thread-safe.</p>
 */
public final class ConfigurableExceptionPolicy implements ExceptionPolicy {

    /**
     * Immutable baseline of exception types that are always permitted.
     */
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

    /**
     * Complete immutable set of allowed exception types, consisting of the
     * baseline plus any configured additions.
     */
    private final Set<Class<?>> allowed;

    /**
     * Creates a new exception policy using the baseline exception set and
     * additional exception classes specified by fully-qualified class name.
     *
     * @param additionalClassNames
     *        collection of fully-qualified exception class names to allow,
     *        or {@code null} if no additional exceptions are configured
     *
     * @throws IllegalArgumentException
     *         if any configured class:
     *         <ul>
     *           <li>cannot be found</li>
     *           <li>does not extend {@link Throwable}</li>
     *           <li>extends {@link Error}</li>
     *         </ul>
     */
    public ConfigurableExceptionPolicy(Collection<String> additionalClassNames) {
        Set<Class<?>> set = new HashSet<Class<?>>(BASELINE);

        if (additionalClassNames != null) {
            for (String name : additionalClassNames) {
                set.add(resolve(name));
            }
        }

        this.allowed = Collections.unmodifiableSet(set);
    }

    /**
     * Determines whether the given exception type is permitted by this policy.
     *
     * <p>An exception is allowed if it is assignable to any of the configured
     * allowed types. This permits subclasses of allowed exceptions.</p>
     *
     * @param type
     *        the concrete exception class to test
     *
     * @return {@code true} if the exception type is allowed;
     *         {@code false} otherwise
     */
    @Override
    public boolean isAllowed(Class<?> type) {
        for (Class<?> allowedType : allowed) {
            if (allowedType.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves and validates a configured exception class name.
     *
     * <p>The resolved class must extend {@link Throwable} and must not
     * extend {@link Error}. Errors are explicitly disallowed to prevent
     * propagation of fatal JVM conditions.</p>
     *
     * @param className
     *        fully-qualified exception class name
     *
     * @return the resolved exception class
     *
     * @throws IllegalArgumentException
     *         if the class cannot be resolved or violates policy constraints
     */
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
