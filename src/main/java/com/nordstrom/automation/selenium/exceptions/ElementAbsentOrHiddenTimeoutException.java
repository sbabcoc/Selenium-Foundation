package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the {@link Coordinators#visibilityOf(org.openqa.selenium.WebElement)},
 * {@link Coordinators#visibilityOfElementLocated(org.openqa.selenium.By)}, and
 * {@link Coordinators#visibilityOfAnyElementLocated(org.openqa.selenium.By)} conditions and indicates that the
 * specified element was still absent or hidden when the timeout interval expired.
 */
public class ElementAbsentOrHiddenTimeoutException extends TimeoutException {

    private static final long serialVersionUID = -2295630523192380636L;

    public ElementAbsentOrHiddenTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
