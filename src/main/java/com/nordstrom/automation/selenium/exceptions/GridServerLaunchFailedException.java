package com.nordstrom.automation.selenium.exceptions;

/**
 * Thrown if a Grid component process failed to start.
 */
public class GridServerLaunchFailedException extends RuntimeException {

    private static final long serialVersionUID = 5186366410431999078L;
    private static final String TEMPLATE = "Failed to start grid %s process";

    /**
     * Constructor for {@code launch failed} exception with the specified server role.
     * 
     * @param role Grid server role specifier ({@code hub} or {@code node})
     * @param cause the cause of this exception
     */
    public GridServerLaunchFailedException(final String role, final Throwable cause) {
        super(getMessage(role), cause);
    }
    
    /**
     * Get exception message for the specified server role.
     * 
     * @param role Grid server role specifier ({@code hub} or {@code node})
     * @return exception message for the specified server role
     */
    private static String getMessage(final String role) {
        return String.format(TEMPLATE, role);
    }
}
