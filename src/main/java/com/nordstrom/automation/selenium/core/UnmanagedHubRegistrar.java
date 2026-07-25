package com.nordstrom.automation.selenium.core;

import java.net.URL;

import com.nordstrom.automation.selenium.SeleniumConfig;

/**
 * Registrar invoked whenever a client connects to an already-active Selenium Grid
 * hub that the current process did not create, allowing the connection to be
 * tracked without taking on any ownership of the hub's lifecycle.
 * <p>
 * Registered via {@link SeleniumGrid#registerUnmanagedHubRegistrar(UnmanagedHubRegistrar)},
 * normally by a {@link GridMonitorPlugin} implementation's static initializer.
 * <p>
 * Implementations must:
 * <ul>
 *     <li>Never attempt to shut down, stop, or otherwise take ownership of the
 *         referenced hub — this hook exists purely for tracking/visibility;</li>
 *     <li>Return promptly and swallow/log any failure — a registrar failure must
 *         never prevent or interrupt the caller's own Grid connection;</li>
 *     <li>Be safe to invoke repeatedly for the same hub URL (idempotent).</li>
 * </ul>
 *
 * @since [next-major]
 */
@FunctionalInterface
public interface UnmanagedHubRegistrar {
    /**
     * Handle a client connection to the specified active, unmanaged hub.
     *
     * @param config {@link SeleniumConfig} object
     * @param hubUrl {@link URL} of the unmanaged hub that was connected to
     */
    void onConnect(SeleniumConfig config, URL hubUrl);
}
