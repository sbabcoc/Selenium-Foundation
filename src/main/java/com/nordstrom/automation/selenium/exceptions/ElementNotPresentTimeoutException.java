package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the {@link Coordinators#presenceOfElementLocated(org.openqa.selenium.By)}
 * condition and indicates that no elements matching the specified locator were found within the timeout interval.
 */
public class ElementNotPresentTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 7039645286156391657L;

    /**
     * Constructor for a new "element not present" timeout exception with
     * the specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ElementNotPresentTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
