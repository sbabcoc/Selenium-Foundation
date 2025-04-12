package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the {@link Coordinators#newWindowIsOpened(java.util.Set)} condition and
 * indicates that no new browser window appeared within the timeout interval.
 */
public class NoWindowAppearedTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 1178667313414119377L;

    /**
     * Constructor for a new "no window appeared" timeout exception with
     * the specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public NoWindowAppearedTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
