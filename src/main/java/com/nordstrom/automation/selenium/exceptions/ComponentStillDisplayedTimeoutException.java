package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.model.PageComponent;

/**
 * This exception is associated with the {@link PageComponent#componentIsHidden()} condition and indicates that the
 * indicated page component was still displayed when the timeout interval expired.
 */
public class ComponentStillDisplayedTimeoutException extends TimeoutException { //NOSONAR

    private static final long serialVersionUID = 5397614393701035129L;

    public ComponentStillDisplayedTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
