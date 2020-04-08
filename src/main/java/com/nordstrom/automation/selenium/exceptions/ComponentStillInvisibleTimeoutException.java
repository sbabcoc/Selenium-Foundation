package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.model.PageComponent;

/**
 * This exception is associated with the {@link PageComponent#componentIsVisible()} condition and indicates that the
 * indicated page component was still invisible when the timeout interval expired.
 */
public class ComponentStillInvisibleTimeoutException extends TimeoutException { //NOSONAR

    private static final long serialVersionUID = 7779370358344583623L;

    public ComponentStillInvisibleTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
