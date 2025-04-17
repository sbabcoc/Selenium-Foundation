package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the {@link Coordinators#stalenessOf(org.openqa.selenium.WebElement)} condition
 * and indicates that the specified element reference failed to go "stale" within the timeout interval.
 */
public class ElementStillFreshTimeoutException extends TimeoutException {

    private static final long serialVersionUID = -3082528281757446744L;

    /**
     * Constructor for a new "element still fresh" timeout exception with
     * the specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ElementStillFreshTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
