package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the
 * {@link Coordinators#elementToHaveAttributeValue(org.openqa.selenium.By, String, String)} and
 * and {@link Coordinators#textToBePresentInElementValue(org.openqa.selenium.By, String)} conditions and indicates
 * that the indicated attribute of the specified element failed to attain the specified value within the timeout
 * interval.
 */
public class ElementAttributeTimeoutException extends TimeoutException { //NOSONAR

    private static final long serialVersionUID = 7422856432865870480L;

    public ElementAttributeTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
