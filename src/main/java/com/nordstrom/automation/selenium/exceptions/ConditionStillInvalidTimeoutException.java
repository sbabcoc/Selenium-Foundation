package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the {@link Coordinators#has(java.util.function.Function)} wrapper method
 * and indicates that the wrapped condition was still returning a 'negative' result when the timeout interval expired. 
 */
public class ConditionStillInvalidTimeoutException extends TimeoutException {

    private static final long serialVersionUID = -5012103332012897882L;

    /**
     * Constructor for a new "component still invalid" timeout exception with
     * the specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ConditionStillInvalidTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
