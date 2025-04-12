package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.model.ContainerMethodInterceptor;

/**
 * This exception is associated with {@link ContainerMethodInterceptor#intercept(Object, Method, Object[], Callable)}
 * and indicates that the browser timed out waiting for web page resources to be processed and rendered.
 */
public class PageLoadRendererTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 2076867125637147698L;

    /**
     * Constructor for a new "page load renderer" timeout exception with the
     * specified message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public PageLoadRendererTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
