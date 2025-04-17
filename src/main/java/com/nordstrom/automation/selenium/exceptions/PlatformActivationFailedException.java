package com.nordstrom.automation.selenium.exceptions;

import org.apache.commons.lang3.StringUtils;

import com.nordstrom.automation.selenium.platform.PlatformEnum;
import com.nordstrom.automation.selenium.platform.PlatformTargetable;

/**
 * This exception is thrown by implementation of the {@link PlatformTargetable#activatePlatform activatePlatform}
 * method to indicate that platform activation failed.
 * 
 */
public class PlatformActivationFailedException extends RuntimeException {

    private static final long serialVersionUID = 7336291801605667538L;
    private static final String TEMPLATE = "Failed to activate target platform '%s'";

    /**
     * Constructor for {@code platform activation failed} exception with the specified platform and optional details.
     * 
     * @param platform platform to be activated
     * @param details [optional] message details
     */
    public PlatformActivationFailedException(final PlatformEnum platform, final String... details) {
        super(getMessage(platform, details));
    }
    
    /**
     * Constructor for {@code platform activation failed} exception with the specified platform, underlying cause, and
     * optional details.
     * 
     * @param platform platform to be activated
     * @param cause underlying cause for activation failure
     * @param details [optional] message details
     */
    public PlatformActivationFailedException(
            final PlatformEnum platform, final Throwable cause, final String... details) {
        super(getMessage(platform, details), cause);
    }
    
    /**
     * Get exception message to the specified platform with optional details.
     * 
     * @param platform platform to be activated
     * @param details [optional] message details
     * @return exception message to the specified platform
     */
    private static String getMessage(final PlatformEnum platform, String... details) {
        String appendix = (details.length == 0) ? "" : "\n" + StringUtils.join(details, "\n");
        return String.format(TEMPLATE, platform.getName()) + appendix;
    }

}
