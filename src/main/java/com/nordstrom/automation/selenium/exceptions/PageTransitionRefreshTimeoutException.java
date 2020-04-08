package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.model.ContainerMethodInterceptor;

/**
 * This exception is associated with {@link ContainerMethodInterceptor#intercept(Object, Method, Object[], Callable)}
 * and indicates that the parent page reference element failed to go "stale" within the timeout interval.
 */
public class PageTransitionRefreshTimeoutException extends TimeoutException { //NOSONAR

    private static final long serialVersionUID = 6396536840195276179L;

    public PageTransitionRefreshTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
