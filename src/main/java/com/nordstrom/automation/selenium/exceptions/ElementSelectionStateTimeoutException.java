package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the
 * {@link Coordinators#elementSelectionStateToBe(org.openqa.selenium.By, boolean)} condition and indicates that the
 * specified element failed to attain the indicated selection state within the timeout interval.
 */
public class ElementSelectionStateTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 2150778933322672061L;

    /**
     * Constructor for a new "element selection state" timeout exception with
     * the specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ElementSelectionStateTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
