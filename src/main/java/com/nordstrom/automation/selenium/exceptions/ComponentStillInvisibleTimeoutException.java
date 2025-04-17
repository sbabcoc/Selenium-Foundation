package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.model.PageComponent;

/**
 * This exception is associated with the {@link PageComponent#componentIsVisible()} condition and indicates that the
 * indicated page component was still invisible when the timeout interval expired.
 */
public class ComponentStillInvisibleTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 7779370358344583623L;

    /**
     * Constructor for a new "component still invisible" timeout exception with
     * the specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ComponentStillInvisibleTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
