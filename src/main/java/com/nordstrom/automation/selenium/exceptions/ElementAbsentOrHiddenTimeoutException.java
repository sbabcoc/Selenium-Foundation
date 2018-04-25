package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

/**
 * This exception is associated with the {@link Waits#elementDisplayed(org.openqa.selenium.By)} and
 * {@link Waits#elementDisplayed(org.openqa.selenium.WebElement)} conditions and indicates that the specified element
 * was still absent or hidden when the timeout interval expired.
 */
public class ElementAbsentOrHiddenTimeoutException extends TimeoutException {

    private static final long serialVersionUID = -2295630523192380636L;

    public ElementAbsentOrHiddenTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
