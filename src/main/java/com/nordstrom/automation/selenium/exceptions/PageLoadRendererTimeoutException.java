package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

/**
 * This exception is associated with {@link PageMethodInterceptor#intercept(Object, Method, Object[], MethodProxy)}
 * and indicates that the browser timed out waiting for web page resources to be processed and rendered.
 */
public class PageLoadRendererTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 2076867125637147698L;

    public PageLoadRendererTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
