package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.examples.TextEditApplication;

/**
 * This exception is associated with the {@link TextEditApplication#newDocumentIsOpened(java.util.Set)} condition and
 * indicates that no new document window appeared within the timeout interval.
 */
public class NoDocumentAppearedTimeoutException extends TimeoutException {

    private static final long serialVersionUID = -2764521081752602572L;

	/**
     * Constructor for a new "no document appeared" timeout exception with
     * the specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public NoDocumentAppearedTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
