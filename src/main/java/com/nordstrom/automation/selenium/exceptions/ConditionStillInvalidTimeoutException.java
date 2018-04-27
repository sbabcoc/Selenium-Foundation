package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the {@link Coordinators#has(com.google.common.base.Function)} wrapper method
 * and indicates that the wrapped condition was still returning a 'negative' result when the timeout interval expired. 
 */
public class ConditionStillInvalidTimeoutException extends TimeoutException { //NOSONAR

    private static final long serialVersionUID = -5012103332012897882L;

    public ConditionStillInvalidTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
