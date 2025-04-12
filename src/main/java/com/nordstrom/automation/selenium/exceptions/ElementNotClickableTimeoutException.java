package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the {@link Coordinators#elementToBeClickable(org.openqa.selenium.By)} and
 * {@link Coordinators#elementToBeClickable(org.openqa.selenium.WebElement)} conditions and indicates that the
 * specified element failed to become click-able within the timeout interval.
 */
public class ElementNotClickableTimeoutException extends TimeoutException {

    private static final long serialVersionUID = -8475358618203763123L;

    /**
     * Constructor for a new "element not clickable" timeout exception with
     * the specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ElementNotClickableTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
