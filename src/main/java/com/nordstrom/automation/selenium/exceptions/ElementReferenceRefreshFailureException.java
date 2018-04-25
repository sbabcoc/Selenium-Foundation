package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.StaleElementReferenceException;

/**
 * This exception is associated with {@link RobustWebElement#refresh(org.openqa.selenium.StaleElementReferenceException)}
 * and indicates that the attempt to refresh a stale element reference was unsuccessful.
 */
public class ElementReferenceRefreshFailureException extends StaleElementReferenceException {

    private static final long serialVersionUID = 417132799562814181L;

    public ElementReferenceRefreshFailureException(String message, Throwable cause) {
		super(message, cause);
	}

}
