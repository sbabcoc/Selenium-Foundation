package com.nordstrom.automation.selenium.utility;

import org.openqa.selenium.net.NetworkUtils;

/**
 * This class serves as a shim to access {@link NetworkUtils#getIp4NonLoopbackAddressOfThisMachine()}.
 */
public class HostUtils {
    
    private static final NetworkUtils IDENTITY = new NetworkUtils();
    
    /**
     * Private constructor to prevent instantiation.
     */
    private HostUtils() {
        throw new AssertionError("HostUtils is a static utility class that cannot be instantiated");
    }
    
    /**
     * Get Internet protocol (IP) address for the machine we're running on.
     * 
     * @return IP address for the machine we're running on (a.k.a. - 'localhost')
     */
    public static String getLocalHost() {
        return IDENTITY.getIp4NonLoopbackAddressOfThisMachine().getHostAddress();
    }    
}
