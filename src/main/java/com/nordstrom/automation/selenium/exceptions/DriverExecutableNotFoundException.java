package com.nordstrom.automation.selenium.exceptions;

public class DriverExecutableNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -5718589545720652315L;
    
    private static final String PATH_DEFINED = "Driver executable neither found at '%s' (specified by [%s]) nor on the PATH";
    private static final String PATH_OMITTED = "Driver executable not found on the PATH; add it or specify location in [%s]";

    public DriverExecutableNotFoundException(String driverPathProp) {
        super(getMessage(driverPathProp));
    }
    
    private static String getMessage(final String driverPathProp) {
        String driverPath = System.getProperty(driverPathProp);
        if (driverPath != null) {
            return String.format(PATH_DEFINED, driverPath, driverPathProp);
        } else {
            return String.format(PATH_OMITTED, driverPathProp);
        }
    }
    
}
