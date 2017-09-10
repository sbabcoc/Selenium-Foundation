package com.nordstrom.automation.selenium.exceptions;

import com.nordstrom.automation.selenium.listeners.DriverManager;

/**
 * This exception is thrown by {@link DriverManager#getInitialPage} if no initial page was specified.
 */
public class InitialPageNotSpecifiedException extends RuntimeException {

    private static final long serialVersionUID = -6182879162513331011L;

    /**
     * Constructor for exception with default message.
     */
    public InitialPageNotSpecifiedException() {
        super("No initial page has been specified");
    }
}
