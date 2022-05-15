package com.nordstrom.automation.selenium.exceptions;

/**
 * This exception is thrown when a client calls a method of a container object that's no longer valid.
 */
public class ContainerVacatedException extends RuntimeException {
    
    private static final long serialVersionUID = -7653982501901130765L;
    
    public ContainerVacatedException(VacationStackTrace stackTrace) {
        super(stackTrace);
    }

}
