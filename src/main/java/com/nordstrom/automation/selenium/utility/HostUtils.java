package com.nordstrom.automation.selenium.utility;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(HostUtils.class);

    private static volatile String cachedHost = resolveLocalHost();

    private HostUtils() {
        throw new AssertionError("HostUtils is a static utility class that cannot be instantiated");
    }

    /**
     * Get the Internet protocol (IP) address for the machine we're running on.
     * <p>
     * The resolved address is cached, but the cache is validated on every call:
     * if the previously-resolved address is no longer bound to any active network
     * interface (e.g. after a Wi-Fi disconnect/reconnect), it's re-resolved. This
     * validation is a cheap local interface lookup, not a full re-enumeration, so
     * it's safe to call on every request.
     *
     * @return IP address for the machine we're running on
     */
    public static String getLocalHost() {
        String host = cachedHost;
        if (isStillBound(host)) {
            return host;
        }
        synchronized (HostUtils.class) {
            host = cachedHost;
            if (!isStillBound(host)) {
                LOGGER.debug("Cached local host '{}' is no longer bound to any active interface; re-resolving", host);
                host = resolveLocalHost();
                cachedHost = host;
            }
        }
        return host;
    }

    /**
     * Determine whether the specified address is still bound to an active network interface.
     *
     * @param address IP address to check
     * @return {@code true} if the address is still bound to an active interface
     */
    private static boolean isStillBound(String address) {
        try {
            return NetworkInterface.getByInetAddress(InetAddress.getByName(address)) != null;
        } catch (SocketException | UnknownHostException e) {
            return false;
        }
    }

    /**
     * Resolve the preferred local IP address of this machine.
     * <p>
     * Called once at class load time, and again by {@link #getLocalHost()} whenever
     * the previously-resolved address is found to no longer be bound to an active
     * interface. On macOS, checks {@code en0} directly to avoid a slow reverse DNS lookup.
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
