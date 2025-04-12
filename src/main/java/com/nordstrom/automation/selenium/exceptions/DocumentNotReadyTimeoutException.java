package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.core.JsUtility;

/**
 * This exception is associated with the {@link JsUtility#documentIsReady()} condition and indicates that the
 * current document failed to become ready within the timeout interval.
 */
public class DocumentNotReadyTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 3611395001046784941L;

    /**
     * Constructor for a new "document not ready" timeout exception with the
     * specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public DocumentNotReadyTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
