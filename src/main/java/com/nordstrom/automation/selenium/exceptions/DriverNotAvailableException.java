package com.nordstrom.automation.selenium.exceptions;

import com.nordstrom.automation.selenium.listeners.DriverManager;

/**
 * This exception is thrown by {@link DriverManager#getDriver} when no driver is available.
 */
public class DriverNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = 657965846077748022L;

    /**
     * Constructor for exception with default message.
     */
    public DriverNotAvailableException() {
        super("No driver was found in the current test context");
    }
}
