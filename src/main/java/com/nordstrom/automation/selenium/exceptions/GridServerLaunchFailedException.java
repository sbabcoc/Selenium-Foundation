package com.nordstrom.automation.selenium.exceptions;

/**
 * 
 */
public class GridServerLaunchFailedException extends RuntimeException {

    private static final long serialVersionUID = 5186366410431999078L;

    /**
     * 
     * @param message
     * @param cause
     */
    public GridServerLaunchFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
