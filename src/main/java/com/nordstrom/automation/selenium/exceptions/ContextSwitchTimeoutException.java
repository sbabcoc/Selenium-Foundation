package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.model.ComponentContainer;

/**
 * This exception is associated with the {@link ComponentContainer#switchTo()} condition and indicates that the
 * driver could not be switched to the indicated container's search context before the timeout interval expired.
 * <p>
 * This covers every reason {@code switchToContext()} may fail to complete - including, but not limited to, an
 * optional context element that never became available; the underlying cause remains available via
 * {@link #getCause()}.
 */
public class ContextSwitchTimeoutException extends TimeoutException {

    private static final long serialVersionUID = -6203247881263957602L;

    /**
     * Constructor for a new "context switch" timeout exception with
     * the specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ContextSwitchTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
