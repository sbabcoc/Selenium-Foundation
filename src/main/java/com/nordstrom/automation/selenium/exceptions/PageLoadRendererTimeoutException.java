package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.model.ContainerMethodInterceptor;

/**
 * This exception is associated with {@link ContainerMethodInterceptor#intercept(Object, Method, Object[], Callable)}
 * and indicates that the browser timed out waiting for web page resources to be processed and rendered.
 */
public class PageLoadRendererTimeoutException extends TimeoutException { //NOSONAR

    private static final long serialVersionUID = 2076867125637147698L;

    public PageLoadRendererTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
