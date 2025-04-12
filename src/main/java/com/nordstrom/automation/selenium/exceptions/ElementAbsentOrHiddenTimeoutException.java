package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the {@link Coordinators#visibilityOf(org.openqa.selenium.WebElement)},
 * {@link Coordinators#visibilityOfElementLocated(org.openqa.selenium.By)}, and
 * {@link Coordinators#visibilityOfAnyElementLocated(org.openqa.selenium.By)} conditions and indicates that the
 * specified element was still absent or hidden when the timeout interval expired.
 */
public class ElementAbsentOrHiddenTimeoutException extends TimeoutException {

    private static final long serialVersionUID = -2295630523192380636L;

    /**
     * Constructor for a new "element absent or hidden" timeout exception with
     * the specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ElementAbsentOrHiddenTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
