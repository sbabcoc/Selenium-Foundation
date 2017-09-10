package com.nordstrom.automation.selenium.exceptions;

import org.apache.http.HttpHost;

public class InvalidGridHostException extends RuntimeException {

    private static final long serialVersionUID = -3037697283479571401L;
    private static final String TEMPLATE = "Specified Selenium Grid %s host '%s' is malformed";

    /**
     * 
     * @param role
     * @param host
     * @param cause
     */
    public InvalidGridHostException(String role, HttpHost host, Throwable cause) {
        super(getMessage(role, host), cause);
    }
    
    private static String getMessage(String role, HttpHost host) {
        return String.format(TEMPLATE, role, host.toURI());
    }
}
