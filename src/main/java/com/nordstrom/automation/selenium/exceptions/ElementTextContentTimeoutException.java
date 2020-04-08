package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the
 * {@link Coordinators#textToBePresentInElementLocated(org.openqa.selenium.By, String)} and
 * {@link Coordinators#textToNotBeEmptyInElementLocated(org.openqa.selenium.By)} conditions and indicates that the
 * specified element failed to attain the indicated text content within the timeout interval.
 */
public class ElementTextContentTimeoutException extends TimeoutException { //NOSONAR

    private static final long serialVersionUID = -2893297898946904937L;

    public ElementTextContentTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
