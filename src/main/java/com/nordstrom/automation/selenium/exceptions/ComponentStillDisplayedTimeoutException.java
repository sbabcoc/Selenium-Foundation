package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.model.PageComponent;

/**
 * This exception is associated with the {@link PageComponent#componentIsHidden()} condition and indicates that the
 * indicated page component was still displayed when the timeout interval expired.
 */
public class ComponentStillDisplayedTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 5397614393701035129L;

    /**
     * Constructor for a new "component still displayed" timeout exception with
     * the specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ComponentStillDisplayedTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
