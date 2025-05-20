package com.nordstrom.automation.selenium.utility;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.openqa.selenium.net.NetworkInterfaceProvider;
import org.openqa.selenium.net.NetworkUtils;

/**
 * This class serves as a shim to access {@link NetworkUtils#getIp4NonLoopbackAddressOfThisMachine()}.
 * To work around a <b>Selenium 3</b> defect, <b>HostUtils</b> initializes <b>NetworkUtils</b> with a copy of
 * the <b>Selenium 4</b> version of {@link DefaultNetworkInterfaceProviderV4 DefaultNetworkInterfaceProvider}.
 */
public class HostUtils {
    
    private static final NetworkUtils IDENTITY;
    
    static {
        NetworkUtils identity;
        try {
            Constructor<NetworkUtils> ctor = NetworkUtils.class.getDeclaredConstructor(NetworkInterfaceProvider.class);
            ctor.setAccessible(true);
            identity = ctor.newInstance(new DefaultNetworkInterfaceProviderV4());
        } catch (NoSuchMethodException | SecurityException | InstantiationException |
                IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            identity = new NetworkUtils();
        }
        IDENTITY = identity;
    }
    
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
