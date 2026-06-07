package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.openqa.selenium.Capabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.plugins.PluginUtils;
import com.nordstrom.common.uri.UriUtils;

/**
 * <b>The {@code SeleniumGrid} Object</b>
 * <p>
 * The <b>{@code SeleniumGrid}</b> object provides an interface to 
 * <a href='https://github.com/SeleniumHQ/selenium/wiki/Grid2'>Selenium Grid</a> collections - both local and remote.
 * A standard grid object is available through the configuration, and independent instances can be created as needed.
 * <p>
 * <b>Using the standard {@code SeleniumGrid} object</b>
 * <p>
 * By default, <b>Selenium Foundation</b> acquires its browser sessions from an instance of the 
 * <a href='https://seleniumhq.github.io/docs/grid.html'>Selenium Grid</a>. If no remote Grid instance is specified in
 * your project's configuration, <b>Selenium Foundation</b> will launch and manage a local instance for you.
 * <p>
 * As stated in the main 
 * <a href='https://github.com/sbabcoc/Selenium-Foundation/blob/master/README.md#grid-based-driver-creation'>README
 * </a> file, <b>Selenium Foundation</b> acquires local browser sessions from a local Grid instance to avoid divergent
 * behavior and special-case code to support both local and remote operation.
 */
public class SeleniumGrid {
    
    static final int CONNECT_TIMEOUT_MS = 500;

    /** hub server of this Grid instance */
    protected IGridServer hubServer;
    /** node servers of this Grid instance */
    protected Map<URL, IGridServer> nodeServers = new HashMap<>();
    /** "personalities" supported by this Grid instance */
    protected Map<String, String> personalities = new HashMap<>();
    /** SLF4J logger for this Selenium Grid model */
    protected static final Logger LOGGER = LoggerFactory.getLogger(SeleniumGrid.class);
    
    private static IGridServerFactory GRID_SERVER_FACTORY = (url, isHub) -> new GridServer(url, isHub);
    
    private static java.util.function.BiFunction<SeleniumConfig, URL, SeleniumGrid> LOCAL_GRID_FACTORY =
            (config, hubUrl) -> { throw new IllegalStateException(
                "No local Grid factory registered - ensure selenium-grid-manager is on the classpath"); };
    
    /**
     * No-argument constructor for subclasses. 
     */
    protected SeleniumGrid() { }
    
    /**
     * Constructor for models of Selenium Grid instances from hub URL.
     * <p>
     * This is used to create an interface for an active grid - remote or local.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubUrl {@link URL} for grid hub host
     * @throws IOException if unable to acquire Grid details
     */
    public SeleniumGrid(SeleniumConfig config, URL hubUrl) throws IOException {
        hubServer = GRID_SERVER_FACTORY.createServer(hubUrl, true);
        List<URL> nodeEndpoints = GridServer.getGridProxies(config, hubUrl);
        if (nodeEndpoints.isEmpty()) {
            LOGGER.debug("Detected existing servlet container at: {}", hubUrl);
        } else {
            LOGGER.debug("Mapping structure of existing grid at: {}", hubUrl);
            for (URL nodeEndpoint : nodeEndpoints) {
                URI nodeUri = UriUtils.uriForPath(nodeEndpoint, GridServer.HUB_BASE);
                nodeServers.put(nodeEndpoint, GRID_SERVER_FACTORY.createServer(nodeUri.toURL(), false));
                addNodePersonalities(config, hubServer.getUrl(), nodeEndpoint);
            }
            LOGGER.debug("{}: Personalities => {}", hubServer.getUrl(), personalities.keySet());
        }
    }
    
    /**
     * Add supported personalities of the specified Grid node.
     * <p>
     * <b>NOTE</b>: Names of node personalities are derived from the following capabilities
     * (in order of precedence):
     * 
     * <ul>
     *     <li><b>automationName</b>: 'appium' automation name</li>
     *     <li><b>browserName</b>: name of target browser</li>
     * </ul>
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubUrl {@link URL} of Grid hub
     * @param nodeUrl node endpoint URL
     * @throws IOException if an I/O error occurs
     */
    private void addNodePersonalities(SeleniumConfig config, URL hubUrl, URL nodeUrl) throws IOException {
        LOGGER.debug("{}: Adding personalities of node: {}", hubUrl, nodeUrl);
        List<Capabilities> capabilitiesList = GridServer.getNodeCapabilities(config, hubUrl, nodeUrl);
        for (Capabilities capabilities : capabilitiesList) {
            personalities.putAll(PluginUtils.getPersonalitiesForBrowser(GridUtility.getPersonality(capabilities)));
        }
    }
    
    /**
     * Register the factory used to create {@link IGridServer} instances.
     * <p>
     * <b>NOTE</b>: This method overrides the default factory, which creates
     * plain {@link GridServer} instances without process lifecycle management.
     * The registered factory typically creates {@code LocalGridServer} instances
     * that support shutdown via port-based process discovery.
     *
     * @param factory {@link IGridServerFactory} object
     * @throws NullPointerException if {@code factory} is {@code null}
     */
    public static void registerGridServerFactory(IGridServerFactory factory) {
        GRID_SERVER_FACTORY = Objects.requireNonNull(factory, "[factory] must be non-null");
    }
    
    /**
     * Register the factory used to create local {@link SeleniumGrid} instances.
     * <p>
     * <b>NOTE</b>: This method overrides the default factory, which throws
     * {@link IllegalStateException} indicating that {@code selenium-grid-manager}
     * is not on the classpath. The registered factory is called when
     * {@link #create(SeleniumConfig, URL)} is invoked with a local hub URL.
     *
     * @param factory factory function that accepts a {@link SeleniumConfig} and
     *     a hub {@link URL} and returns a {@link SeleniumGrid} instance
     * @throws NullPointerException if {@code factory} is {@code null}
     */
    public static void registerLocalGridFactory(
            java.util.function.BiFunction<SeleniumConfig, URL, SeleniumGrid> factory) {
        LOCAL_GRID_FACTORY = Objects.requireNonNull(factory, "[factory] must be non-null");
    }

    /**
     * Create an object that represents the Selenium Grid with the specified hub endpoint.
     *
     * @param config {@link SeleniumConfig} object
     * @param hubUrl {@link URL} of hub host
     * @return {@link SeleniumGrid} object for the specified hub endpoint
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if {@code hubUrl} is {@code null}
     * @throws IllegalStateException if no Grid is active at the specified URL
     */
    public static SeleniumGrid create(SeleniumConfig config, URL hubUrl) throws IOException {
        Objects.requireNonNull(config, "[config] must be non-null");
        Objects.requireNonNull(hubUrl, "[hubUrl] must be non-null");

        if (GridUtility.isLocalHost(hubUrl)) {
            return LOCAL_GRID_FACTORY.apply(config, hubUrl);
        }

        if (GridServer.isHubActive(hubUrl)) {
            System.setProperty(SeleniumSettings.HUB_HOST.key(), hubUrl.toExternalForm());
            System.setProperty(SeleniumSettings.HUB_PORT.key(), Integer.toString(hubUrl.getPort()));
            return new SeleniumGrid(config, hubUrl);
        }

        throw new IllegalStateException("No active Selenium Grid found at: " + hubUrl);
    }
    
    /**
     * Activate this local <b>Grid</b> instance.
     * <p>
     * This method ensures that the hub and node servers associated with this local grid are launched and active,
     * and it also ensures that grid node servers are registered with the hub. 
     * 
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if this thread was interrupted
     * @throws TimeoutException if host timeout interval exceeded
     */
    public void activate() throws IOException, InterruptedException, TimeoutException {
        // no-op
    }
    
    /**
     * Determine if this Selenium Grid is active.
     *
     * @return {@code true} if grid is active; otherwise {@code false}
     */
    public boolean isActive() {
        return (hubServer != null) && hubServer.isActive();
    }
    
    /**
     * Shutdown the Selenium Grid represented by this object.
     * 
     * @return {@code false} if non-local Grid server encountered; otherwise {@code true}
     * @throws InterruptedException if this thread was interrupted
     */
    public boolean shutdown() throws InterruptedException {
        if (!isActive()) return true;
        
        boolean result = true;
        Iterator<Entry<URL, IGridServer>> iterator = nodeServers.entrySet().iterator();
        
        // shutdown node servers
        while (iterator.hasNext()) {
            Entry<URL, IGridServer> serverEntry = iterator.next();
            // if shutdown of this node succeeds
            if (serverEntry.getValue().shutdown()) {
                iterator.remove();
            } else {
                result = false;
            }
        }
        
        // if all nodes shutdown
        if (result) {
            // if hub shutdown succeeds
            if (hubServer.shutdown()) {
                hubServer = null;
            } else {
                result = false;
            }
        }
        
        return result;
     }
    
    /**
     * Get grid server object for the active hub.
     * 
     * @return {@link GridServer} object that represents the active hub server
     */
    public IGridServer getHubServer() {
        return hubServer;
    }
    
    /**
     * Get the map of grid server objects for the attached nodes.
     * 
     * @return map of {@link GridServer} objects that represent the attached node servers
     */
    public Map<URL, IGridServer> getNodeServers() {
        return nodeServers;
    }
    
    /**
     * Get capabilities object for the specified browser personality.
     * 
     * @param config {@link SeleniumConfig} object
     * @param personality browser personality to retrieve
     * @return {@link Capabilities} object for the specified personality
     * @throws IllegalArgumentException if specified personality isn't supported by the active Grid
     */
    public Capabilities getPersonality(SeleniumConfig config, String personality) {
        if (personality == null) throw new IllegalArgumentException("[personality] must be non-null");
        String json = personalities.get(personality);
        if ((json == null) || json.isEmpty()) {
            String message = String.format("Specified personality '%s' not supported by active Grid", personality);
            String browserName = personality.split("\\.")[0];
            if ( ! browserName.equals(personality)) {
                LOGGER.warn("{}; revert to browser name '{}'", message, browserName);
                Capabilities[] capsList = config.getCapabilitiesForName(browserName);
                if (capsList.length > 0) {
                    return capsList[0];
                }
            }
            throw new IllegalArgumentException(message);
        } else {
            return config.getCapabilitiesForJson(json)[0];
        }
    }
}
