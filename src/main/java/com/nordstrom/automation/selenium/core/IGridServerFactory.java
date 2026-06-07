package com.nordstrom.automation.selenium.core;

import java.net.URL;

/**
 * This interface defines the contract for version-specific Grid server factory objects.
 * Implementations are provided by {@code selenium-grid-manager} and registered via
 * {@link SeleniumGrid#registerGridServerFactory(IGridServerFactory)}.
 * <p>
 * When registered, the factory is used by {@link SeleniumGrid} to create hub and node
 * server objects that support proper lifecycle management, including shutdown via
 * port-based process discovery.
 */
public interface IGridServerFactory {

    /**
     * Create a Grid server object for the specified URL.
     *
     * @param url server {@link URL}
     * @param isHub {@code true} for hub; {@code false} for node
     * @return {@link IGridServer} object for the specified server
     */
    IGridServer createServer(URL url, boolean isHub);
}
