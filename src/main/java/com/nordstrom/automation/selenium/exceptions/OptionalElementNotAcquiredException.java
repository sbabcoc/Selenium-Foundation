package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * This exception is thrown upon failing to acquire the reference for an optional element prior to invoking a
 * {@link WebElement} method.
 */
public class OptionalElementNotAcquiredException extends RuntimeException {

    private static final long serialVersionUID = -1817241270199904930L;
    
    /**
     * Constructor for {@code not acquired} exception.
     * 
     * @param cause the cause of this exception
     */
    public OptionalElementNotAcquiredException(final NoSuchElementException cause) {
        super("Unable to acquire reference for optional element", cause);
    }

}
