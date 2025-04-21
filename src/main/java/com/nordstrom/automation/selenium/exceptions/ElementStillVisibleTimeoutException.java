package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the {@link Coordinators#invisibilityOfElementLocated(org.openqa.selenium.By)}
 * condition and indicates that the specified element was still visible when the timeout interval expired.
 */
public class ElementStillVisibleTimeoutException extends TimeoutException {

    private static final long serialVersionUID = -3777087787464228714L;

    /**
     * Constructor for a new "element still visible" timeout exception with
     * the specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ElementStillVisibleTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
