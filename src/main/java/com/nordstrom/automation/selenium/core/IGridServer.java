package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * This interface defines the contract for Selenium Grid server objects.
 * <p>
 * Implementations include both local server processes and remote server references.
 */
public interface IGridServer {

    /**
     * Get the URL for this server.
     *
     * @return {@link URL} object for this server
     */
    URL getUrl();

    /**
     * Determine if this Grid server is a hub host.
     *
     * @return {@code true} if this server is a hub; otherwise {@code false}
     */
    boolean isHub();

    /**
     * Determine if this server is active.
     *
     * @return {@code true} if server is active; otherwise {@code false}
     */
    boolean isActive();

    /**
     * Start this Grid server.
     * <p>
     * <b>NOTE</b>: For non-local servers this is a no-op.
     *
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if this thread was interrupted
     * @throws TimeoutException if host timeout interval exceeded
     */
    default void start() throws IOException, InterruptedException, TimeoutException {
        // no-op
    }

    /**
     * Stop this Grid server.
     * <p>
     * <b>NOTE</b>: This default implementation returns {@code false} since non-local
     * Grid servers cannot be shut down by the framework. Local Grid servers override
     * this method to terminate their associated process.
     *
     * @return {@code false} if server is non-local; {@code true} if successfully shut down
     * @throws InterruptedException if this thread was interrupted
     */
    default boolean shutdown() throws InterruptedException {
        return false;
    }

    /**
     * Get the driver 'personalities' for this Grid server.
     *
     * @return map of personality name to desired capabilities JSON
     */
    default Map<String, String> getPersonalities() {
        return Collections.emptyMap();
    }
}
