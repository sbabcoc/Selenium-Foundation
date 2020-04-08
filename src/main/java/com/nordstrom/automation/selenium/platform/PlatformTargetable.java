package com.nordstrom.automation.selenium.platform;

import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.exceptions.PlatformActivationFailedException;
import com.nordstrom.common.file.PathUtils.PathModifier;

/**
 * Test classes that implement this interface are afforded the ability to specify the platform(s) supported by each
 * of its test methods. Each test method can specify one or more target platform on which it should run. At run-time,
 * the test flow controller determines which test methods should run on the current target platform, only including
 * those test methods that support that platform.
 */
public interface PlatformTargetable<P extends Enum<?> & PlatformEnum> extends PathModifier {
    
    /**
     * Get the target platform for this test class instance.
     * 
     * @return target platform for this instance
     */
    P getTargetPlatform();
    
    /**
     * Activate the specified target platform.
     * 
     * @param driver WebDriver object
     * @param platform platform to be activated
     * @throws PlatformActivationFailedException if platform activation fails
     */
    void activatePlatform(WebDriver driver, P platform) throws PlatformActivationFailedException;
    
    /**
     * Get the collection of valid platforms.
     * 
     * @return array of valid platform constants
     */
    P[] getValidPlatforms();
    
    /**
     * Get the default platform specifier.
     * 
     * @return default platform constant
     */
    P getDefaultPlatform();
    
    /**
     * Convert the specified platform name to the corresponding constant.
     * 
     * @param name platform name
     * @return platform constant; 'null' for unsupported names
     */
    P platformFromString(String name);
    
    /**
     * Get data type of platform enumeration.
     * 
     * @return data type of platform enumeration
     */
    Class<P> getPlatformType();
    
}
