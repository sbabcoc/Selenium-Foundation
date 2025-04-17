package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.model.ContainerMethodInterceptor;

/**
 * This exception is associated with {@link ContainerMethodInterceptor#intercept(Object, Method, Object[], Callable)}
 * and indicates that the parent page reference element failed to go "stale" within the timeout interval.
 */
public class PageTransitionRefreshTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 6396536840195276179L;

    /**
     * Constructor for a new "page transition refresh" timeout exception with
     * the specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public PageTransitionRefreshTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
