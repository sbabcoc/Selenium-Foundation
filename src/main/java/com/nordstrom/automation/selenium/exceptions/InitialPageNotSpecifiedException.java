package com.nordstrom.automation.selenium.exceptions;

/**
 * 
 */
public class InitialPageNotSpecifiedException extends RuntimeException {

    private static final long serialVersionUID = -6182879162513331011L;

    /**
     * 
     * @param message
     */
    public InitialPageNotSpecifiedException(String message) {
        super(message);
    }
}
