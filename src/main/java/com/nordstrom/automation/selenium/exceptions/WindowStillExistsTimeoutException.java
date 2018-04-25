package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

/**
 * This exception is associated with the {@link Waits#windowToVanish(String)} condition and indicates that the browser
 * window with the specified window handle was still present when the timeout interval expired.
 */
public class WindowStillExistsTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 1228040448300937511L;

    public WindowStillExistsTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
