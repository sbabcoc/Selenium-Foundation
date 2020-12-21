package com.nordstrom.automation.selenium.core;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.grid.common.GridRole;
import org.openqa.selenium.Capabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.common.base.UncheckedThrow;

/**
 * <h1>The {@code SeleniumGrid} Object</h1>
 * <p>
 * The <b>{@code SeleniumGrid}</b> object provides an interface to 
 * <a href='https://github.com/SeleniumHQ/selenium/wiki/Grid2'>Selenium Grid</a> collections - both local and remote.
 * A standard grid object is available through the configuration, and independent instances can be created as needed.
 * 
 * <h2>Using the standard {@code SeleniumGrid} object</h2>
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
@SuppressWarnings("squid:S1774")
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
    private Map<String, GridServer> nodeServers = new HashMap<>();
    protected Map<String, String> personalities = new HashMap<>();
    
    protected static final Logger LOGGER = LoggerFactory.getLogger(SeleniumGrid.class);
    
    /**
     * Constructor for Selenium Grid from hub URL.
     * <p>
     * This is used to create an interface for an active grid - remote or local.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubUrl {@link URL} for grid hub host
     * @throws IOException if unable to acquire Grid details
     */
    public SeleniumGrid(SeleniumConfig config, URL hubUrl) throws IOException {
        hubServer = new GridServer(hubUrl, GridRole.HUB);
        for (String nodeEndpoint : GridUtility.getGridProxies(hubUrl)) {
            URL nodeUrl = new URL(nodeEndpoint + GridServer.HUB_BASE);
            nodeServers.put(nodeEndpoint, new GridServer(nodeUrl, GridRole.NODE));
            addNodePersonalities(config, hubServer.getUrl(), nodeEndpoint);
        }
        addPluginPersonalities();
    }
    
    /**
     * Constructor for Selenium Grid from server objects.
     * <p>
     * This is used to create an interface for a newly-created local Grid.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubServer {@link GridServer} object for hub host
     * @param nodeServers array of {@link GridServer} objects for node hosts
     * @throws IOException if unable to acquire Grid details
     */
    public SeleniumGrid(SeleniumConfig config, GridServer hubServer, GridServer... nodeServers) throws IOException {
        this.hubServer = Objects.requireNonNull(hubServer);
        if (Objects.requireNonNull(nodeServers).length == 0) {
            throw new IllegalArgumentException("[nodeServers] must be non-empty");
        }
        for (GridServer nodeServer : nodeServers) {
            String nodeEndpoint = "http://" + nodeServer.getUrl().getAuthority();
            this.nodeServers.put(nodeEndpoint, nodeServer);
            addNodePersonalities(config, hubServer.getUrl(), nodeEndpoint);
        }
        addPluginPersonalities();
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
     * @param nodeEndpoint node endpoint
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void addNodePersonalities(SeleniumConfig config, URL hubUrl, String nodeEndpoint) throws IOException {
        for (Capabilities capabilities : GridUtility.getNodeCapabilities(config, hubUrl, nodeEndpoint)) {
            Map<String, Object> req = (Map<String, Object>) capabilities.getCapability("request");
            List<Map> capsList = (List<Map>) req.get("capabilities");
            if (capsList == null) {
                Map<String, Object> conf = (Map<String, Object>) req.get("configuration");
                capsList = (List<Map>) conf.get("capabilities");
            }
            for (Map<String, Object> capsItem : capsList) {
                String personalityName = (String) capsItem.get("automationName");
                if (personalityName == null) {
                    personalityName = (String) capsItem.get("browserName");
                }
                personalities.put(personalityName, config.toJson(capsItem));
            }
        }
    }
    
    /**
     * Add supported personalities from configured driver plug-ins.
     */
    private void addPluginPersonalities() {
        for (DriverPlugin driverPlugin : ServiceLoader.load(DriverPlugin.class)) {
            if (personalities.containsKey(driverPlugin.getBrowserName())) {
                personalities.putAll(driverPlugin.getPersonalities());
            }
        }
    }
    
    /**
     * Create an object that represents the Selenium Grid with the specified hub endpoint.
     * <p>
     * If the endpoint is {@code null} or specifies an inactive {@code localhost} URL, this method launches a local
     * Grid instance and returns a {@link LocalSeleniumGrid} object.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubUrl {@link URL} of hub host
     * @return {@link SeleniumGrid} object for the specified hub endpoint
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if this thread was interrupted
     * @throws TimeoutException if host timeout interval exceeded
     */
    public static SeleniumGrid create(SeleniumConfig config, URL hubUrl) throws IOException, InterruptedException, TimeoutException {
        if (GridUtility.isHubActive(hubUrl)) {
            return new SeleniumGrid(config, hubUrl);
        } else if ((hubUrl == null) || GridUtility.isLocalHost(hubUrl)) {
            if (hubUrl != null) {
                // ensure that hub port is available as a discrete setting
                System.setProperty(SeleniumSettings.HUB_PORT.key(), Integer.toString(hubUrl.getPort()));
            }
            return LocalSeleniumGrid.launch(config, config.getHubConfigPath());
        }
        throw new IllegalStateException("Specified remote hub URL '" + hubUrl + "' isn't active");
    }
    
    /**
     * Shutdown the Selenium Grid represented by this object.
     * 
     * @param localOnly {@code true} to target only local Grid servers
     * @return {@code false} if non-local Grid server encountered; otherwise {@code true}
     * @throws InterruptedException if this thread was interrupted
     */
    public boolean shutdown(final boolean localOnly) throws InterruptedException {
        boolean result = true;
        Iterator<Entry<String, GridServer>> iterator = nodeServers.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Entry<String, GridServer> serverEntry = iterator.next();
            if (serverEntry.getValue().shutdown(localOnly)) {
                iterator.remove();
            } else {
                result = false;
            }
        }
        
        if (hubServer.shutdown(localOnly)) {
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
    public Map<String, GridServer> getNodeServers() {
        return nodeServers;
    }
    
    /**
     * Get capabilities object for the specified browser personality.
     * 
     * @param config {@link SeleniumConfig} object
     * @param personality browser personality to retrieve
     * @return {@link Capabilities} object for the specified personality
     */
    public Capabilities getPersonality(SeleniumConfig config, String personality) {
        String json = personalities.get(personality);
        if ((json == null) || json.isEmpty()) {
            String message = String.format("Specified personality '%s' not supported by local Grid", personality);
            String browserName = personality.split("\\.")[0];
            if ( ! browserName.equals(personality)) {
                LOGGER.warn("{}; revert to browser name '{}'", message, browserName);
                Capabilities[] capsList = config.getCapabilitiesForName(browserName);
                if (capsList.length > 0) {
                    return capsList[0];
                }
            }
            throw new RuntimeException(message);
        } else {
            return config.getCapabilitiesForJson(json)[0];
        }
    }

    public static class GridServer {
        private GridRole role;
        private URL serverUrl;
        protected String statusRequest;
        protected String shutdownRequest;
        
        public static final String GRID_CONSOLE = "/grid/console";
        public static final String HUB_BASE = "/wd/hub";
        public static final String NODE_STATUS = "/wd/hub/status";
        public static final String HUB_CONFIG = "/grid/api/hub/";
        public static final String NODE_CONFIG = "/grid/api/proxy";
        
        private static final String HUB_SHUTDOWN = "/lifecycle-manager?action=shutdown";
        private static final String NODE_SHUTDOWN = "/extra/LifecycleServlet?action=shutdown";
        private static final long SHUTDOWN_DELAY = 15;
        
        public GridServer(URL url, GridRole role) {
            this.role = role;
            this.serverUrl = url;
            if (isHub()) {
                statusRequest = HUB_CONFIG;
                shutdownRequest = HUB_SHUTDOWN;
            } else {
                statusRequest = NODE_STATUS;
                shutdownRequest = NODE_SHUTDOWN;
            }
        }
        
        /**
         * Determine if this Grid server is a hub host.
         * 
         * @return {@code true} if this server is a hub; otherwise {@code false}
         */
        public boolean isHub() {
            return (role == GridRole.HUB);
        }
        
        /**
         * Get the URL for this server.
         * 
         * @return {@link URL} object for this server
         */
        public URL getUrl() {
            return serverUrl;
        }
        
        /**
         * Stop the Selenium Grid server represented by this object.
         * 
         * @param localOnly {@code true} to target only local Grid server
         * @return {@code false} if [localOnly] and server is remote; otherwise {@code true}
         * @throws InterruptedException if this thread was interrupted
         */
        public boolean shutdown(final boolean localOnly) throws InterruptedException {
            return shutdown(serverUrl, statusRequest, shutdownRequest, localOnly);
        }

        /**
         * Stop the specified Selenium Grid server.
         * 
         * @param serverUrl Selenium server URL
         * @param statusRequest Selenium server status request
         * @param shutdownRequest Selenium server shutdown request
         * @param localOnly {@code true} to target only local Grid server
         * @return {@code false} if [localOnly] and server is remote; otherwise {@code true}
         * @throws InterruptedException if this thread was interrupted
         */
        public static boolean shutdown(final URL serverUrl, final String statusRequest,
                        final String shutdownRequest, final boolean localOnly) throws InterruptedException {
            
            if (localOnly && !GridUtility.isLocalHost(serverUrl)) {
                return false;
            }
            
            if (GridUtility.isHostActive(serverUrl, statusRequest)) {
                try {
                    GridUtility.getHttpResponse(serverUrl, shutdownRequest);
                    waitUntilUnavailable(SHUTDOWN_DELAY, TimeUnit.SECONDS, serverUrl);
                    Thread.sleep(1000);
                } catch (IOException | org.openqa.selenium.net.UrlChecker.TimeoutException e) {
                    throw UncheckedThrow.throwUnchecked(e);
                }
            }
            
            return true;
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
