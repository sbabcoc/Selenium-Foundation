package com.nordstrom.automation.selenium.interfaces;

import com.nordstrom.automation.selenium.model.ComponentContainer;

/**
 * This interface defines the method implemented by Selenium Foundation transition error detectors. These detectors are
 * registered via a ServiceLoader provider configuration file. Registered detectors are notified whenever a container
 * method returns a new container object.
 */
public interface TransitionErrorDetector {
    
    /**
     * Scan the specified container context for transition errors.
     * 
     * @param context container context to scan for errors
     * @return error message string; {@code null} if no errors are detected
     */
    String scanForErrors(ComponentContainer context);

}
