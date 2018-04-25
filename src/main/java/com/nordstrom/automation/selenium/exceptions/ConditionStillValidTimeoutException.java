package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

/**
 * This exception is associated with the {@link Waits#not(com.google.common.base.Function)} wrapper method
 * and indicates that the wrapped condition was still returning a 'positive' result when the timeout interval expired. 
 */
public class ConditionStillValidTimeoutException extends TimeoutException {

	private static final long serialVersionUID = -7734612321697213740L;

	public ConditionStillValidTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
