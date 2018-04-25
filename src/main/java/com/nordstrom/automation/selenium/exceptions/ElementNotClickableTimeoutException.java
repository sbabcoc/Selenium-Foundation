package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

/**
 * This exception is associated with the {@link Waits#elementToBeClickable(org.openqa.selenium.By)} and
 * {@link Waits#elementToBeClickable(org.openqa.selenium.WebElement)} conditions and indicates that the
 * specified element failed to become click-able within the timeout interval.
 */
public class ElementNotClickableTimeoutException extends TimeoutException {

	public ElementNotClickableTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	private static final long serialVersionUID = -774138381198597605L;

}
