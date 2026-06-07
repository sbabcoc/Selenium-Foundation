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
     * Create a Grid server object for the specified hub URL.
     *
     * @param url hub server {@link URL}
     * @return {@link IGridServer} object for the specified hub
     */
    IGridServer createHubServer(URL url);

    /**
     * Create a Grid server object for the specified node URL.
     *
     * @param url node server {@link URL}
     * @return {@link IGridServer} object for the specified node
     */
    IGridServer createNodeServer(URL url);
}
