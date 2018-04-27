package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the {@link Coordinators#not(com.google.common.base.Function)} wrapper method
 * and indicates that the wrapped condition was still returning a 'positive' result when the timeout interval expired. 
 */
public class ConditionStillValidTimeoutException extends TimeoutException { //NOSONAR

    private static final long serialVersionUID = -1194280527172574112L;

    public ConditionStillValidTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
