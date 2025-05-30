package com.nordstrom.automation.selenium.core;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.Capabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.LocalSeleniumGrid.LocalGridServer;
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
    private static final int READ_TIMEOUT_MS = 1000;
    private static final long MAX_POLL_INTERVAL_MS = 320;
    private static final long MIN_POLL_INTERVAL_MS = 10;

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(
        new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "UrlChecker-" + THREAD_COUNTER.incrementAndGet()); // Thread safety reviewed
                t.setDaemon(true);
                return t;
            }
        });

    private GridServer hubServer;
    private Map<URL, GridServer> nodeServers = new HashMap<>();
    
    /** "personalities" supported by this Grid instance */
    protected Map<String, String> personalities = new HashMap<>();
    /** SLF4J logger for this Selenium Grid model */
    protected static final Logger LOGGER = LoggerFactory.getLogger(SeleniumGrid.class);
    
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
        hubServer = new GridServer(hubUrl, true);
        List<URL> nodeEndpoints = GridServer.getGridProxies(config, hubUrl);
        if (nodeEndpoints.isEmpty()) {
            LOGGER.debug("Detected existing servlet container at: {}", hubUrl);
        } else {
            LOGGER.debug("Mapping structure of existing grid at: {}", hubUrl);
            for (URL nodeEndpoint : nodeEndpoints) {
                URI nodeUri = UriUtils.uriForPath(nodeEndpoint, GridServer.HUB_BASE);
                nodeServers.put(nodeEndpoint, new GridServer(nodeUri.toURL(), false));
                addNodePersonalities(config, hubServer.getUrl(), nodeEndpoint);
            }
            LOGGER.debug("{}: Personalities => {}", hubServer.getUrl(), personalities.keySet());
        }
    }
    
    /**
     * Constructor for Selenium Grid from server objects.
     * <p>
     * This is used to create an interface for a newly-created local Grid.<br>
     * <b>NOTE</b>: The represented local Grid instance is <b>NOT</b> immediately activated. Activation is performed
     * by {@link GridUtility#getDriver()} the first time a supported driver is requested.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubServer {@link LocalGridServer} object for hub host
     * @param nodeServers array of {@link LocalGridServer} objects for node hosts
     * @throws IOException if unable to acquire Grid details
     * @see GridUtility#getDriver()
     * @see LocalSeleniumGrid#activate()
     */
    public SeleniumGrid(SeleniumConfig config, LocalGridServer hubServer, LocalGridServer... nodeServers) throws IOException {
        this.hubServer = Objects.requireNonNull(hubServer, "[hubServer] must be non-null");
        if (nodeServers.length > 0) {
            LOGGER.debug("Assembling graph of pending grid at: {}", hubServer.getUrl());
            for (LocalGridServer nodeServer : nodeServers) {
                String nodeEndpoint = nodeServer.getUrl().getProtocol() + "://" + nodeServer.getUrl().getAuthority();
                URL nodeUrl = URI.create(nodeEndpoint).toURL();
                this.nodeServers.put(nodeUrl, nodeServer);
                this.personalities.putAll(nodeServer.getPersonalities());
            }
            LOGGER.debug("{}: Personalities => {}", hubServer.getUrl(), personalities.keySet());
        } else if (config.getVersion() == 3) {
            LOGGER.debug("Queued up servlet container at: {}", hubServer.getUrl());
        } else {
            LOGGER.debug("Queued up hub without nodes at: {}", hubServer.getUrl());
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
     * Create an object that represents the Selenium Grid with the specified hub endpoint.
     * <p>
     * If the endpoint is {@code null} or specifies an inactive {@code localhost} URL, this method launches a local
     * Grid instance and returns a {@link LocalSeleniumGrid} object.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubUrl {@link URL} of hub host (may be {@code null})
     * @return {@link SeleniumGrid} object for the specified hub endpoint
     * @throws IOException if an I/O error occurs
     */
    public static SeleniumGrid create(SeleniumConfig config, URL hubUrl) throws IOException {
        Objects.requireNonNull(config, "[config] must be non-null");
        
        // if URL is undefined or specifies 'localhost' address
        if (hubUrl == null || GridUtility.isLocalHost(hubUrl)) {
            // create/augment local grid instance
            return LocalSeleniumGrid.create(config, hubUrl);
        // otherwise, if URL responds to requests
        } else if (GridServer.isHubActive(hubUrl)) {
            // store hub host and hub port in system properties for subsequent retrieval
            System.setProperty(SeleniumSettings.HUB_HOST.key(), hubUrl.toExternalForm());
            System.setProperty(SeleniumSettings.HUB_PORT.key(), Integer.toString(hubUrl.getPort()));
            // build graph of existing grid
            return new SeleniumGrid(config, hubUrl);
        }
        
        throw new IllegalStateException("Specified remote hub URL '" + hubUrl + "' isn't active");
    }
    
    /**
     * Shutdown the Selenium Grid represented by this object.
     * 
     * @return {@code false} if non-local Grid server encountered; otherwise {@code true}
     * @throws InterruptedException if this thread was interrupted
     */
    public boolean shutdown() throws InterruptedException {
        boolean result = true;
        Iterator<Entry<URL, GridServer>> iterator = nodeServers.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Entry<URL, GridServer> serverEntry = iterator.next();
            if (serverEntry.getValue().shutdown()) {
                iterator.remove();
            } else {
                result = false;
            }
        }
        
        if (hubServer.shutdown()) {
            hubServer = null;
        } else {
            result = false;
        }
        
        return result;
     }
    
    /**
     * Get grid server object for the active hub.
     * 
     * @return {@link GridServer} object that represents the active hub server
     */
    public GridServer getHubServer() {
        return hubServer;
    }
    
    /**
     * Get the map of grid server objects for the attached nodes.
     * 
     * @return map of {@link GridServer} objects that represent the attached node servers
     */
    public Map<URL, GridServer> getNodeServers() {
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

    /**
     * Wait up to the specified interval for the indicated URL(s) to be available.
     * <p>
     * <b>NOTE</b>: This method was back-ported from the {@link org.openqa.selenium.net.UrlChecker UrlChecker} class in
     * Selenium 3 to compile under Java 7.
     * 
     * @param timeout timeout interval
     * @param unit granularity of specified timeout
     * @param urls URLs to poll for availability
     * @throws org.openqa.selenium.net.UrlChecker.TimeoutException if indicated URL is still available after specified
     *     interval.
     */
    public static void waitUntilAvailable(long timeout, TimeUnit unit, final URL... urls)
            throws org.openqa.selenium.net.UrlChecker.TimeoutException {
        long start = System.nanoTime();
        try {
            Future<Void> callback = EXECUTOR.submit(new Callable<Void>() {
                public Void call() throws InterruptedException {
                    HttpURLConnection connection = null;

                    long sleepMillis = MIN_POLL_INTERVAL_MS;
                    while (true) {
                        if (Thread.interrupted()) {
                            throw new InterruptedException();
                        }
                        for (URL url : urls) {
                            try {
                                connection = connectToUrl(url);
                                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                    return null;
                                }
                            } catch (IOException e) {
                                // Ok, try again.
                            } finally {
                                if (connection != null) {
                                    connection.disconnect();
                                }
                            }
                        }
                        MILLISECONDS.sleep(sleepMillis);
                        sleepMillis = (sleepMillis >= MAX_POLL_INTERVAL_MS) ? sleepMillis : sleepMillis * 2;
                    }
                }
            });
            callback.get(timeout, unit);
        } catch (java.util.concurrent.TimeoutException e) {
            throw new org.openqa.selenium.net.UrlChecker.TimeoutException(
                    String.format("Timed out waiting for %s to be available after %d ms", Arrays.toString(urls),
                            MILLISECONDS.convert(System.nanoTime() - start, NANOSECONDS)),
                    e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wait up to the specified interval for the indicated URL to be unavailable.
     * <p>
     * <b>NOTE</b>: This method was back-ported from the {@link org.openqa.selenium.net.UrlChecker UrlChecker} class in
     * Selenium 3 to compile under Java 7.
     * 
     * @param timeout timeout interval
     * @param unit granularity of specified timeout
     * @param url URL to poll for availability
     * @throws org.openqa.selenium.net.UrlChecker.TimeoutException if indicated URL is still available after specified
     *     interval.
     */
    public static void waitUntilUnavailable(long timeout, TimeUnit unit, final URL url)
                    throws org.openqa.selenium.net.UrlChecker.TimeoutException {
        long start = System.nanoTime();
        try {
            Future<Void> callback = EXECUTOR.submit(new Callable<Void>() {
                public Void call() throws InterruptedException {
                    HttpURLConnection connection = null;

                    long sleepMillis = MIN_POLL_INTERVAL_MS;
                    while (true) {
                        try {
                            connection = connectToUrl(url);
                            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                                return null;
                            }
                        } catch (IOException e) {
                            return null;
                        } finally {
                            if (connection != null) {
                                connection.disconnect();
                            }
                        }

                        MILLISECONDS.sleep(sleepMillis);
                        sleepMillis = (sleepMillis >= MAX_POLL_INTERVAL_MS) ? sleepMillis
                                        : sleepMillis * 2;
                    }
                }
            });
            callback.get(timeout, unit);
        } catch (TimeoutException e) {
            throw new org.openqa.selenium.net.UrlChecker.TimeoutException(String.format(
                            "Timed out waiting for %s to become unavailable after %d ms", url,
                            MILLISECONDS.convert(System.nanoTime() - start, NANOSECONDS)), e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a connection to the specified URL.
     * <p>
     * <b>NOTE</b>: This method was lifted from the {@link org.openqa.selenium.net.UrlChecker UrlChecker} class in the
     * Selenium API.
     * 
     * @param url URL for connection
     * @return connection to the specified URL
     * @throws IOException if an I/O exception occurs
     */
    private static HttpURLConnection connectToUrl(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.connect();
        return connection;
    }
}
