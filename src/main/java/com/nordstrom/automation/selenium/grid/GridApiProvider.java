package com.nordstrom.automation.selenium.grid;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.openqa.selenium.Capabilities;

/**
 * Version-specific provider for Selenium Grid API interactions.
 * <p>
 * Implementations are registered via {@link java.util.ServiceLoader} and provide
 * grid interaction capabilities appropriate for a specific Selenium API version.
 * The registry is maintained by {@link GridApiProviderRegistry}, which selects
 * the appropriate provider based on the detected API version of each grid instance.
 * <p>
 * This design future-proofs grid interaction — supporting a new Selenium version
 * requires only a new implementation registered via {@code META-INF/services},
 * with no changes to existing code.
 *
 * @since [next-major]
 */
public interface GridApiProvider {

    /**
     * Get the Selenium API version this provider supports.
     *
     * @return supported API version (e.g. 3 or 4)
     */
    int getApiVersion();

    /**
     * Determine if the specified URL identifies an active Grid hub
     * of the version supported by this provider.
     *
     * @param hubUrl {@link URL} to check
     * @return {@code true} if the URL identifies an active hub of the supported version;
     *         otherwise {@code false}
     */
    boolean isHub(URL hubUrl);

    /**
     * Get the URLs of nodes registered with the specified Grid hub.
     *
     * @param hubUrl {@link URL} of Grid hub
     * @return list of node {@link URL} objects; empty list if none found or on error
     * @throws IOException if an I/O error occurs
     */
    List<URL> getNodeUrls(URL hubUrl) throws IOException;

    /**
     * Determine if the specified node is registered with the specified hub.
     *
     * @param hubUrl {@link URL} of Grid hub
     * @param nodeUrl {@link URL} of Grid node
     * @return {@code true} if the node is registered; otherwise {@code false}
     */
    boolean isNodeRegistered(URL hubUrl, URL nodeUrl);

    /**
     * Get the capabilities of the specified node registered with the specified hub.
     *
     * @param hubUrl {@link URL} of Grid hub
     * @param nodeUrl {@link URL} of Grid node
     * @return list of {@link Capabilities} objects for the specified node;
     *         empty list if node not found or on error
     * @throws IOException if an I/O error occurs
     */
    List<Capabilities> getNodeCapabilities(URL hubUrl, URL nodeUrl) throws IOException;
}
