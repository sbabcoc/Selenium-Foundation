package com.nordstrom.automation.selenium.exceptions;

import org.apache.http.HttpHost;

/**
 * This exception is thrown to indicate that the Selenium Grid host specification is malformed.
 */
public class InvalidGridHostException extends RuntimeException {

    private static final long serialVersionUID = -3037697283479571401L;
    private static final String TEMPLATE = "Specified Selenium Grid %s host '%s' is malformed";

    /**
     * Constructor for {@code invalid host} exception with the specified role and host.
     * 
     * @param role Grid server role specifier ({@code hub} or {@code node})
     * @param host Grid server host specifier
     * @param cause the cause of this exception
     */
    public InvalidGridHostException(String role, HttpHost host, Throwable cause) {
        super(getMessage(role, host), cause);
    }
    
    /**
     * Get exception message for the specified role and host.
     * 
     * @param role Grid server role specifier ({@code hub} or {@code node})
     * @param host Grid server host specifier
     * @return exception message for the specified role and host
     */
    private static String getMessage(String role, HttpHost host) {
        return String.format(TEMPLATE, role, host.toURI());
    }
}
