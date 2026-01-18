package com.nordstrom.automation.selenium.exceptions;

import com.nordstrom.automation.selenium.model.ShadowRoot;

/**
 * This exception is throw during instantiation of {@link ShadowRoot} page components if the indicated root
 * element is not a shadow host or has 'closed' shadow-DOM mode.
 */
public class ShadowRootContextException extends SeleniumFoundationException {

    private static final long serialVersionUID = -2655316241833901377L;
    
    /**
     * Constructor for {@code shadow root context} exception.
     */
    public ShadowRootContextException() {
        super("Context is not a shadow host or has 'closed' shadow-DOM mode");
    }

    /**
     * Constructor for {@code shadow root context} exception.
     * 
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public ShadowRootContextException(final String message) {
        super(message);
    }

}
