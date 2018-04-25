package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

/**
 * This exception is associated with {@link PageMethodInterceptor#intercept(Object, Method, Object[], MethodProxy)}
 * and indicates that the parent page reference element failed to go "stale" within the timeout interval.
 */
public class PageTransitionRefreshTimeoutException extends TimeoutException {

	private static final long serialVersionUID = 2006827712348698991L;

	public PageTransitionRefreshTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
