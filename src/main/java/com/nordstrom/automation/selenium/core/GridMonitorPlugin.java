package com.nordstrom.automation.selenium.core;

/**
 * This interface defines the service provider contract for remote Grid monitor implementations.
 * <p>
 * Implementations register a remote hub registrar with {@link SeleniumGrid} via a
 * static initializer. Unlike {@link GridManagerPlugin}, which manages the lifecycle of
 * locally-launched Grid instances, monitor implementations only track remote hubs that
 * clients connect to but do not own or control. The {@link java.util.ServiceLoader}
 * mechanism ensures the implementation is loaded and the registrar registered before any
 * Grid operations occur.
 * <p>
 * To register an implementation, create a provider configuration file at:
 * <pre>META-INF/services/com.nordstrom.automation.selenium.core.GridMonitorPlugin</pre>
 */
public interface GridMonitorPlugin {
    // marker interface - implementations register remote hub registrar in static initializer
}
