package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

/**
 * This exception is associated with the {@link Waits#elementNotDisplayed(org.openqa.selenium.By)}, 
 * {@link Waits#elementNotDisplayed(org.openqa.selenium.WebElement)}, and
 * {@link Waits#invisibilityOfElementLocated(org.openqa.selenium.By)} conditions and indicates that the specified
 * element was still visible when the timeout interval expired.
 */
public class ElementStillVisibleTimeoutException extends TimeoutException {

    private static final long serialVersionUID = -3777087787464228714L;

    public ElementStillVisibleTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
