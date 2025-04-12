package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the {@link Coordinators#windowIsClosed(String)} condition and indicates that the
 * browser window with the specified window handle was still present when the timeout interval expired.
 */
public class WindowStillExistsTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 1228040448300937511L;

    /**
     * Constructor for a new "window still exists" timeout exception with
     * the specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public WindowStillExistsTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
