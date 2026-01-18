package com.nordstrom.automation.selenium.exceptions;

import com.nordstrom.automation.selenium.interfaces.ContainerResolver;

/**
 * This exception is thrown if specified container resolver cannot be instantiated or if
 * invocation of the {@link ContainerResolver#resolve resolve} method fails.
 */
public class ContainerResolverInvocationException extends SeleniumFoundationException {

    private static final long serialVersionUID = -422498744356462151L;

    /**
     * Constructor for "container resolver invocation" exception with specified detail message
     * and associated cause.
     * 
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method). (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ContainerResolverInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
