package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.core.JsUtility;

/**
 * This exception is associated with the {@link JsUtility#documentIsReady()} condition and indicates that the
 * current document failed to become ready within the timeout interval.
 */
public class DocumentNotReadyTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 3611395001046784941L;

    public DocumentNotReadyTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
