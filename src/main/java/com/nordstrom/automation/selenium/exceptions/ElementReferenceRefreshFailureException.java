package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.StaleElementReferenceException;

import com.nordstrom.automation.selenium.model.RobustElementWrapper;

/**
 * This exception is associated with
 * {@link RobustElementWrapper#refreshReference(org.openqa.selenium.StaleElementReferenceException)}
 * and indicates that the attempt to refresh a stale element reference was unsuccessful.
 */
public class ElementReferenceRefreshFailureException extends StaleElementReferenceException {

    private static final long serialVersionUID = 417132799562814181L;

    /**
     * Constructor for a new "element reference refresh failure" exception with the
     * specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ElementReferenceRefreshFailureException(String message, Throwable cause) {
        super(message, cause);
    }

}
