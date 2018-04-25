package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

/**
 * This exception is associated with the {@link PageComponent#componentDisplayed()} condition and indicates that the
 * indicated page component was still invisible when the timeout interval expired.
 */
public class ComponentStillInvisibleTimeoutException extends TimeoutException {

	private static final long serialVersionUID = -6591008648356123519L;
	
	public ComponentStillInvisibleTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
