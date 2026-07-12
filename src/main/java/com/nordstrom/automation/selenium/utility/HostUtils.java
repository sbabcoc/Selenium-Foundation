package com.nordstrom.automation.selenium.utility;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.nordstrom.common.file.OSInfo;

/**
 * This utility class provides a version-agnostic method for determining the
 * preferred local IP address of the current machine.
 * <p>
 * This implementation replaces the Selenium version-specific {@code NetworkUtils}
 * approach, eliminating dependencies on {@code DefaultNetworkInterfaceProviderV4}
 * and {@code HostIdentifierV4}.
 */
public class HostUtils {

    private static final String LOCAL_HOST = resolveLocalHost();

    private HostUtils() {
        throw new AssertionError("HostUtils is a static utility class that cannot be instantiated");
    }

    /**
     * Get the Internet protocol (IP) address for the machine we're running on.
     *
     * @return IP address for the machine we're running on
     */
    public static String getLocalHost() {
        return LOCAL_HOST;
    }

    /**
     * Resolve the preferred local IP address at class load time.
     * <p>
     * On macOS, checks {@code en0} directly to avoid a slow reverse DNS lookup.
     * On all platforms, skips loopback, virtual, and interfaces without hardware
     * addresses (e.g. macOS {@code utun} interfaces). Falls back to
     * {@code 127.0.0.1} if no suitable address is found.
     *
     * @return preferred local IP address
     */
    private static String resolveLocalHost() {
        // macOS optimization: check en0 directly to avoid slow reverse DNS lookup
        if (OSInfo.getDefault().getType() == OSInfo.OSType.MACINTOSH) {
            try {
                NetworkInterface en0 = NetworkInterface.getByName("en0");
                if (en0 != null && en0.isUp() && !en0.isLoopback()) {
                    Enumeration<InetAddress> addresses = en0.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                            return addr.getHostAddress();
                        }
                    }
                }
            } catch (SocketException e) {
                // fall through to general enumeration
            }
        }

        // general case: enumerate all interfaces
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) return "127.0.0.1";
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                try {
                    // skip interfaces that are down, loopback, virtual,
                    // or lack a hardware address (e.g. macOS utun interfaces)
                    if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) continue;
                    if (iface.getHardwareAddress() == null) continue;
                } catch (SocketException e) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()
                            && !addr.isLinkLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            // fall through to loopback
        }
        return "127.0.0.1";
    }
}
