package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

/**
 * This exception is associated with the {@link PageComponent#componentInvisible()} condition and indicates that the
 * indicated page component was still displayed when the timeout interval expired.
 */
public class ComponentStillDisplayedTimeoutException extends TimeoutException {

	private static final long serialVersionUID = 3799757291228589698L;
	
	public ComponentStillDisplayedTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
