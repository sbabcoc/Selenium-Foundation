package com.nordstrom.automation.selenium.exceptions;

public class UnknownGridHostException extends RuntimeException {

    private static final long serialVersionUID = -3037697283479571401L;
    private static final String TEMPLATE = "Specified Selenium Grid %s host '%s' was not found";

    /**
     * 
     * @param hostName
     * @param cause
     */
    public UnknownGridHostException(String role, String hostName, Throwable cause) {
        super(getMessage(role, hostName), cause);
    }
    
    private static String getMessage(String role, String hostName) {
        return String.format(TEMPLATE, role, hostName);
    }
}
