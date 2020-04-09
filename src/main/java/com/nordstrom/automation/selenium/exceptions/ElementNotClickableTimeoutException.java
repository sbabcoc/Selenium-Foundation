package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the {@link Coordinators#elementToBeClickable(org.openqa.selenium.By)} and
 * {@link Coordinators#elementToBeClickable(org.openqa.selenium.WebElement)} conditions and indicates that the
 * specified element failed to become click-able within the timeout interval.
 */
public class ElementNotClickableTimeoutException extends TimeoutException { //NOSONAR

    private static final long serialVersionUID = -8475358618203763123L;

    public ElementNotClickableTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
