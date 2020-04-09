package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the {@link Coordinators#invisibilityOfElementLocated(org.openqa.selenium.By)}
 * condition and indicates that the specified element was still visible when the timeout interval expired.
 */
public class ElementStillVisibleTimeoutException extends TimeoutException { //NOSONAR

    private static final long serialVersionUID = -3777087787464228714L;

    public ElementStillVisibleTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
