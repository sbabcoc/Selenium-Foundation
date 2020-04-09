package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.TimeoutException;

import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This exception is associated with the {@link Coordinators#newWindowIsOpened(java.util.Set)} condition and
 * indicates that no new browser window appeared within the timeout interval.
 */
public class NoWindowAppearedTimeoutException extends TimeoutException { //NOSONAR

    private static final long serialVersionUID = 1178667313414119377L;

    public NoWindowAppearedTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
