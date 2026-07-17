package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.Capabilities;

import com.nordstrom.automation.selenium.grid.GridApiProvider;
import com.nordstrom.automation.selenium.grid.GridApiProviderRegistry;

/**
 * This class represents a single Selenium Grid server (hub or node).
 */
public class GridServer implements IGridServer {

    private final boolean isHub;
    private final URL serverUrl;

    /** Base path for hub server URLs */
    public static final String HUB_BASE = "/wd/hub";

    /**
     * Constructor for Grid server object.
     *
     * @param url base {@link URL} for Grid server
     * @param isHub role of Grid server being started ({@code true} = hub;
     *        {@code false} = node)
     */
    public GridServer(URL url, boolean isHub) {
        this.isHub = isHub;
        this.serverUrl = url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHub() {
        return isHub;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getUrl() {
        return serverUrl;
    }

    /**
     * {@inheritDoc}
     * <p>
     * For hub servers, delegates to {@link #isHubActive(URL)} via the
     * {@link GridApiProviderRegistry}. For node servers, checks the
     * {@code /wd/hub/status} endpoint directly.
     */
    @Override
    public boolean isActive() {
        if (isHub) {
            return isHubActive(serverUrl);
        }
        return GridUtility.isHostActive(serverUrl, "/wd/hub/status");
    }

    /**
     * Determine if the specified Selenium Grid hub is active.
     * <p>
     * Checks all registered {@link GridApiProvider} implementations, supporting
     * both Selenium 3 and Selenium 4 hubs regardless of which API version the
     * current artifact was built for.
     *
     * @param hubUrl {@link URL} to be checked
     * @return {@code true} if specified hub is active; otherwise {@code false}
     */
    public static boolean isHubActive(URL hubUrl) {
        return GridApiProviderRegistry.forHub(hubUrl) != null;
    }

    /**
     * Determine if the indicated Selenium Grid node is registered with the
     * specified hub.
     *
     * @param hubUrl {@link URL} of hub to query
     * @param nodeUrl {@link URL} of node in question
     * @return {@code true} if indicated node is registered; otherwise {@code false}
     */
    public static boolean isNodeRegistered(URL hubUrl, URL nodeUrl) {
        GridApiProvider provider = GridApiProviderRegistry.forHub(hubUrl);
        return provider != null && provider.isNodeRegistered(hubUrl, nodeUrl);
    }

    /**
     * Get the list of node endpoints attached to the specified Selenium Grid hub.
     *
     * @param hubUrl {@link URL} of Grid hub
     * @return list of node endpoints; empty list if hub not recognized or on error
     * @throws IOException if an I/O error occurs
     */
    public static List<URL> getGridProxies(URL hubUrl) throws IOException {
        GridApiProvider provider = GridApiProviderRegistry.forHub(hubUrl);
        return provider != null ? provider.getNodeUrls(hubUrl) : Collections.emptyList();
    }

    /**
     * Get capabilities of the indicated node of the specified Selenium Grid hub.
     *
     * @param hubUrl {@link URL} of Grid hub
     * @param nodeUrl {@link URL} of Grid node
     * @return list of {@link Capabilities} objects for the specified node;
     *         empty list if hub not recognized or on error
     * @throws IOException if an I/O error occurs
     */
    public static List<Capabilities> getNodeCapabilities(URL hubUrl,
            URL nodeUrl) throws IOException {
        GridApiProvider provider = GridApiProviderRegistry.forHub(hubUrl);
        return provider != null
                ? provider.getNodeCapabilities(hubUrl, nodeUrl)
                : Collections.emptyList();
    }
}
