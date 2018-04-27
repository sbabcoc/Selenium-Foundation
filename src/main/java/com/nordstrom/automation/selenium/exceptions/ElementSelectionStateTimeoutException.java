package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the
 * {@link Coordinators#elementSelectionStateToBe(org.openqa.selenium.By, boolean)} condition and indicates that the
 * specified element failed to attain the indicated selection state within the timeout interval.
 */
public class ElementSelectionStateTimeoutException extends TimeoutException { //NOSONAR

    private static final long serialVersionUID = 2150778933322672061L;

    public ElementSelectionStateTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
