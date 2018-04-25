package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

/**
 * This exception is associated with the {@link Waits#elementSelectionStateToBe(org.openqa.selenium.By, boolean)}
 * condition and indicates that the specified element failed to attain the indicated selection state within the
 * timeout interval.
 */
public class ElementSelectionStateTimeoutException extends TimeoutException {

	private static final long serialVersionUID = 6207844346164982531L;

	public ElementSelectionStateTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
