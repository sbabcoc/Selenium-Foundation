package com.nordstrom.automation.selenium.exceptions;

/**
 * This exception is thrown if the concrete container type cannot be resolved.
 */
public class UnresolvedContainerTypeException extends RuntimeException {

    private static final long serialVersionUID = -422498744356462151L;

    /**
     * Constructor for "unresolved container type" exception with default message.
     */
    public UnresolvedContainerTypeException() {
        super("Concrete container type cannot be resolved");
    }

    /**
     * Constructor for "unresolved container type" exception with specified detail message.
     * 
     * @param message detail message
     */
    public UnresolvedContainerTypeException(String message) {
        super(message);
    }
}
