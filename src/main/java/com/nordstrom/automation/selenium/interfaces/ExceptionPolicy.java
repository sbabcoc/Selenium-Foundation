package com.nordstrom.automation.selenium.interfaces;

/**
 * {@code ExceptionPolicy} defines the contract for determining whether a
 * particular Java exception type is permitted to be instantiated and
 * propagated.
 *
 * <p>This interface is primarily used when converting error information
 * originating from external or untrusted sources (such as JavaScript
 * execution environments) into Java {@link Throwable} instances. Implementations
 * act as a safety mechanism that restricts which exception types may be created
 * and thrown.</p>
 *
 * <p>The policy is based on type compatibility rather than exact class
 * matching. Implementations typically allow subclasses of permitted
 * exception types.</p>
 *
 * <p>Implementations must be thread-safe.</p>
 */
public interface ExceptionPolicy {

    /**
     * Determines whether the specified exception type is allowed by this policy.
     *
     * <p>An exception type is considered allowed if it matches or is a subclass
     * of one of the permitted exception classes defined by the implementation.</p>
     *
     * @param exceptionType
     *        the concrete exception class to evaluate
     *
     * @return {@code true} if the exception type is permitted;
     *         {@code false} otherwise
     */
    boolean isAllowed(Class<?> exceptionType);
}
