package com.nordstrom.automation.selenium.exceptions;

import com.nordstrom.automation.selenium.platform.PlatformEnum;

public class PlatformActivationFailedException extends RuntimeException {

    private static final long serialVersionUID = 7336291801605667538L;
    private static final String TEMPLATE = "Failed to activate target platform '%s'";

    /**
     * Constructor for {@code platform activation failed} exception with the specified platform.
     * 
     * @param platform platform to be activated
     */
    public PlatformActivationFailedException(final PlatformEnum platform) {
        super(getMessage(platform));
    }
    
    /**
     * Get exception message to the specified platform.
     * 
     * @param platform platform to be activated
     * @return exception message to the specified platform
     */
    private static String getMessage(final PlatformEnum platform) {
        return String.format(TEMPLATE, platform.getName());
    }

}
