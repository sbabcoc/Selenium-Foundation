package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

/**
 * This exception is associated with the {@link Waits#textToBePresentInElementLocated(org.openqa.selenium.By, String)}
 * {@link Waits#textToNotBeEmptyInElementLocated(org.openqa.selenium.By)} conditions and indicates that the specified
 * element failed to attain the indicated text content within the timeout interval. 
 */
public class ElementTextContentTimeoutException extends TimeoutException {

	private static final long serialVersionUID = 8381016541789662353L;

	public ElementTextContentTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
