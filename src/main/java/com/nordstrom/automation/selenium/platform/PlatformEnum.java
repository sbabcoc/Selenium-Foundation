package com.nordstrom.automation.selenium.platform;

/**
 * This interface provides common methods for collections of 
 */
public interface PlatformEnum {
    
    /**
     * Get name of platform constant.
     * 
     * @return platform constant name
     */
    String getName();
    
    /**
     * Determine if the specified context platform matches this constant.
     * 
     * @param contextPlatform active context platform
     * @return 'true' if this constant matches the specified context platform; otherwise 'false'
     */
    boolean matches(String contextPlatform);
    
}
