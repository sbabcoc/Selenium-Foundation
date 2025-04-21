package com.nordstrom.automation.selenium.exceptions;

/**
 * This exception is thrown when a client calls a method of a container object that's no longer valid.
 */
public class ContainerVacatedException extends RuntimeException {
    
    private static final long serialVersionUID = -7653982501901130765L;
    
    /**
     * Constructor for a new "component vacated" exception with the specified
     * stack trace.
     *
     * @param  stackTrace execution stack trace for the point at which the
     *         associated container became invalid
     */
    public ContainerVacatedException(VacationStackTrace stackTrace) {
        super(stackTrace);
    }

}
