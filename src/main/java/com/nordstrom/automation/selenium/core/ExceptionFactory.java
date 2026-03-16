package com.nordstrom.automation.selenium.core;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.nordstrom.automation.selenium.exceptions.JavaScriptStackTrace;
import com.nordstrom.automation.selenium.exceptions.ScriptExecutionException;

/**
 * Factory responsible for creating Java {@link Throwable} instances based on
 * exception requests originating from JavaScript execution environments.
 *
 * <p>This class serves as a strict security and validation boundary between
 * untrusted JavaScript code and the Java runtime. JavaScript may request that a
 * specific exception type be raised by providing its fully qualified class name,
 * message, and optional JavaScript stack trace. The factory enforces a whitelist
 * to ensure that only approved exception types can be instantiated.</p>
 *
 * <h2>Baseline Allowances</h2>
 * <p>The following exception categories are always permitted:</p>
 * <ul>
 *   <li>All Selenium exceptions ({@code org.openqa.selenium.**})</li>
 *   <li>All Selenium Foundation exceptions
 *       ({@code com.nordstrom.automation.selenium.exceptions.**})</li>
 *   <li>{@link AssertionError} (explicitly allowed)</li>
 *   <li>{@link RuntimeException} and all subclasses</li>
 * </ul>
 *
 * <h2>Additional Configuration</h2>
 * <p>Additional exception classes or packages may be whitelisted at construction
 * time using exact class names or wildcard package expressions:</p>
 * <ul>
 *   <li>{@code com.foo.BarException} – exact class match</li>
 *   <li>{@code com.foo.*} – all exceptions directly in the package</li>
 *   <li>{@code com.foo.**} – all exceptions in the package and subpackages</li>
 * </ul>
 *
 * <h2>Instantiation Rules</h2>
 * <ul>
 *   <li>If the requested exception defines a {@code (String)} constructor, it is used</li>
 *   <li>Otherwise, a no-argument constructor is used</li>
 *   <li>Instantiation failures result in {@link ScriptExecutionException}</li>
 * </ul>
 *
 * <h2>JavaScript Stack Traces</h2>
 * <p>If provided, the JavaScript stack trace is attached to the resulting
 * exception as a suppressed {@link JavaScriptStackTrace} instance.</p>
 *
 * <p>This class is immutable and thread-safe.</p>
 */
public final class ExceptionFactory {

    private final Set<String> whitelist;

    /**
     * Constructs an {@code ExceptionFactory} with optional additional whitelist
     * entries.
     *
     * <p>Whitelist entries may include:</p>
     * <ul>
     *   <li>Fully qualified exception class names</li>
     *   <li>Package wildcards using {@code *} (package only)</li>
     *   <li>Package subtree wildcards using {@code **}</li>
     * </ul>
     *
     * <p>Baseline whitelist entries are always included and cannot be removed.</p>
     *
     * @param additionalWhitelist
     *        additional exception classes or package rules to allow;
     *        may be {@code null}
     */
    public ExceptionFactory(Set<String> additionalWhitelist) {
        Set<String> set = new HashSet<>();
        // Always allow all Selenium exceptions
        set.add("org.openqa.selenium.**");
        // Always allow all Selenium Foundation exceptions
        set.add("com.nordstrom.automation.selenium.exceptions.**");
        // Add user-specified entries if present
        if (additionalWhitelist != null) {
            set.addAll(additionalWhitelist);
        }
        this.whitelist = Collections.unmodifiableSet(set);
    }

    /**
     * Creates a Java exception based on the requested class name.
     *
     * <p>The following exception types are permitted without explicit whitelisting:</p>
     * <ul>
     *   <li>All Selenium exceptions ({@code org.openqa.selenium.**})</li>
     *   <li>All Selenium Foundation exceptions
     *       ({@code com.nordstrom.automation.selenium.exceptions.**})</li>
     *   <li>{@link AssertionError} (exact class only)</li>
     *   <li>{@link RuntimeException} and all subclasses</li>
     * </ul>
     *
     * <p>All other exception types must be explicitly permitted by the configured
     * whitelist. See {@link ExceptionFactory} for whitelist rule syntax.</p>
     *
     * <p>If the requested class is not a {@link Throwable} subclass, is not found,
     * or cannot be instantiated, a {@link ScriptExecutionException} is thrown
     * instead. The same applies if the requested type is not permitted by the
     * configured policy.</p>
     *
     * <p>If provided, the JavaScript stack trace is attached to the resulting
     * exception as a suppressed {@link JavaScriptStackTrace} instance.</p>
     *
     * @param className
     *        fully qualified exception class name requested by JavaScript
     * @param message
     *        exception message; may be {@code null}
     * @param jsStack
     *        JavaScript stack trace; may be {@code null}
     *
     * @return the instantiated exception
     *
     * @throws NullPointerException
     *         if {@code className} is {@code null}
     * @throws ScriptExecutionException
     *         if the class is not found, is not a {@link Throwable} subclass,
     *         is not permitted by policy, or cannot be instantiated
     */
    public Throwable create(String className, String message, String jsStack) {
        Objects.requireNonNull(className, "className");

        try {
            Class<? extends Throwable> clazz = Class.forName(className).asSubclass(Throwable.class);

            if (clazz != AssertionError.class
                    && !RuntimeException.class.isAssignableFrom(clazz)
                    && !isAllowed(clazz)) {
                throw ScriptExecutionException.policyViolation(
                        className,
                        "Requested exception not allowed by configuration: " + className
                );
            }

            Throwable ex = instantiate(clazz, message);
            attachStack(ex, jsStack);
            return ex;

        } catch (ClassNotFoundException | ClassCastException e) {
            throw ScriptExecutionException.instantiationFailure(
                    className,
                    "Requested exception class not found or invalid: " + className,
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

    /**
     * Determines whether the specified exception class is permitted by the
     * configured whitelist.
     *
     * <p>The whitelist supports three rule forms:</p>
     * <ul>
     *   <li><b>Exact class name</b> — {@code com.foo.BarException}</li>
     *   <li><b>Package-only wildcard</b> — {@code com.foo.*}
     *       (classes directly in the package only)</li>
     *   <li><b>Package subtree wildcard</b> — {@code com.foo.**}
     *       (package and all subpackages)</li>
     * </ul>
     *
     * <p>Rules are evaluated in the order they appear in the whitelist, and
     * evaluation stops at the first match.</p>
     *
     * <p>This method performs string-based matching only; it does not validate
     * whether the class is instantiable or assignable to {@link RuntimeException}.
     * Those checks are performed elsewhere.</p>
     *
     * @param clazz
     *        the exception class to evaluate
     *
     * @return {@code true} if the class matches any whitelist rule;
     *         {@code false} otherwise
     */
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

    /**
     * Instantiates the specified class using reflection.
     *
     * <p>The instantiation strategy is:</p>
     * <ol>
     *   <li>Prefer a public constructor accepting a single {@link String}
     *       argument, using the supplied message</li>
     *   <li>Fall back to a public no-argument constructor if no such constructor
     *       exists</li>
     * </ol>
     *
     * <p>No attempt is made to set a cause; exception chaining is intentionally
     * avoided to prevent unintended information leakage across the JavaScript
     * boundary.</p>
     *
     * @param clazz
     *        concrete class to instantiate
     * @param message
     *        exception message; may be {@code null}
     *
     * @return a newly instantiated exception of the requested type
     *
     * @throws ReflectiveOperationException
     *         if the exception cannot be instantiated due to missing accessible
     *         constructors or reflection errors
     */
    private static <T extends Throwable> T instantiate(
            Class<T> clazz,
            String message) throws ReflectiveOperationException {

        try {
            Constructor<T> ctor = clazz.getConstructor(String.class);
            return ctor.newInstance(message);
        } catch (NoSuchMethodException ignored) {
            return clazz.getConstructor().newInstance();
        }
    }

    /**
     * Attaches a JavaScript stack trace to a Java {@link Throwable}, if present.
     *
     * <p>The JavaScript stack trace is wrapped in a {@link JavaScriptStackTrace}
     * instance and added as a suppressed exception. This preserves the original
     * Java exception semantics while making JavaScript execution context available
     * for diagnostics.</p>
     *
     * <p>If the supplied stack trace is {@code null} or empty, this method performs
     * no action.</p>
     *
     * @param t
     *        the target throwable to enrich
     * @param jsStack
     *        JavaScript stack trace text; may be {@code null}
     */
    private static void attachStack(Throwable t, String jsStack) {
        if (jsStack != null && !jsStack.isEmpty()) {
            t.addSuppressed(new JavaScriptStackTrace(jsStack));
        }
    }
}
