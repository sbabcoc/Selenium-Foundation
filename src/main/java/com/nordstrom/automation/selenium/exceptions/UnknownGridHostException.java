package com.nordstrom.automation.selenium.exceptions;

/**
 * This exception indicates that the configured Selenium Grid server host name couldn't be resolved to an IP address.
 */
public class UnknownGridHostException extends RuntimeException {

    private static final long serialVersionUID = -3037697283479571401L;
    private static final String TEMPLATE = "Specified Selenium Grid %s host '%s' was not found";

    /**
     * Constructor for {@code unknown host} exception for the specified role and host name.
     * 
     * @param role Grid server role specifier ({@code hub} or {@code node})
     * @param hostName Grid server host name
     * @param cause the cause of this exception
     */
    public UnknownGridHostException(String role, String hostName, Throwable cause) {
        super(getMessage(role, hostName), cause);
    }
    
    /**
     * Get exception message to the specified role and host name.
     * 
     * @param role Grid server role specifier ({@code hub} or {@code node})
     * @param hostName Grid server host name
     * @return exception message to the specified role and host name
     */
    private static String getMessage(String role, String hostName) {
        return String.format(TEMPLATE, role, hostName);
    }
}
