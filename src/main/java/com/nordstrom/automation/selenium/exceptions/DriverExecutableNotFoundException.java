package com.nordstrom.automation.selenium.exceptions;

import com.nordstrom.automation.selenium.utility.BinaryFinder;

/**
 * This exception is associated with the {@link BinaryFinder#findDriver(String)} method and indicates that a
 * driver matching the specified capabilities could not be acquired.
 */
public class DriverExecutableNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -5718589545720652315L;
    
    private static final String PATH_DEFINED = "Driver executable neither found at '%s' (specified by [%s]) nor on the PATH";
    private static final String PATH_OMITTED = "Driver executable not found on the PATH; add it or specify location in [%s]";

    /**
     * Constructor for a new "driver executable not found" exception with
     * hints from the specified driver path property.
     *
     * @param  driverPathProp driver path property (may be {@code null}
     */
    public DriverExecutableNotFoundException(String driverPathProp) {
        super(getMessage(driverPathProp));
    }
    
    /**
     * Build the exception message with hints from the specified driver path property.
     * 
     * @param  driverPathProp driver path property (may be {@code null}
     * @return exception message string
     */
    private static String getMessage(final String driverPathProp) {
        String driverPath = System.getProperty(driverPathProp);
        if (driverPath != null) {
            return String.format(PATH_DEFINED, driverPath, driverPathProp);
        } else {
            return String.format(PATH_OMITTED, driverPathProp);
        }
    }
    
}
