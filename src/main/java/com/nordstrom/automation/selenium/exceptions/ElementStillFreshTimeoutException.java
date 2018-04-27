package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the {@link Coordinators#stalenessOf(org.openqa.selenium.WebElement)} condition
 * and indicates that the specified element reference failed to go "stale" within the timeout interval.
 */
public class ElementStillFreshTimeoutException extends TimeoutException { //NOSONAR

    private static final long serialVersionUID = -3082528281757446744L;

    public ElementStillFreshTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
