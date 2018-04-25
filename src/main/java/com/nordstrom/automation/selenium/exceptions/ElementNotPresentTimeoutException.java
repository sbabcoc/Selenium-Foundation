package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

/**
 * This exception is associated with the {@link Waits#presenceOfElementLocated(org.openqa.selenium.By)} and
 * {@link Waits#presenceOfAllElementsLocatedBy(org.openqa.selenium.By)} conditions and indicates that no elements
 * matching the specified locator were found within the timeout interval.
 */
public class ElementNotPresentTimeoutException extends TimeoutException {

	private static final long serialVersionUID = 7873204741351029900L;

	public ElementNotPresentTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
