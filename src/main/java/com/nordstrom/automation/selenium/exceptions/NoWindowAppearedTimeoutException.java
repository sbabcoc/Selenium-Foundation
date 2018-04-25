package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

/**
 * This exception is associated with the {@link Waits#windowToAppearAndSwitchToIt(java.util.Set)} condition and
 * indicates that no new browser window appeared within the timeout interval.
 */
public class NoWindowAppearedTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 1178667313414119377L;

    public NoWindowAppearedTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
