package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

/**
 * This exception is associated with the {@link Waits#elementToHaveAttributeValue(org.openqa.selenium.By, String, String)}
 * and {@link Waits#textToBePresentInElementValue(org.openqa.selenium.By, String)} conditions and indicates that the
 * indicated attribute of the specified element failed to attain the specified value within the timeout interval.
 */
public class ElementAttributeTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 7422856432865870480L;

    public ElementAttributeTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
