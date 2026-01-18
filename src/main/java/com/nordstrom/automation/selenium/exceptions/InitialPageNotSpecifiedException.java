package com.nordstrom.automation.selenium.exceptions;

/**
 * This exception is thrown if no initial page was specified.
 */
public class InitialPageNotSpecifiedException extends SeleniumFoundationException {

    private static final long serialVersionUID = -6182879162513331011L;

    /**
     * Constructor for "initial page not specified" exception with default message.
     */
    public InitialPageNotSpecifiedException() {
        super("No initial page has been specified");
    }
}
