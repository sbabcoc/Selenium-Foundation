package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openqa.grid.common.GridRole;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.net.UrlChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.common.base.UncheckedThrow;

@SuppressWarnings("squid:S1774")
public class SeleniumGrid {
    
    private GridServer hubServer;
    private Map<String, GridServer> nodeServers = new HashMap<>();

    protected static final Logger LOGGER = LoggerFactory.getLogger(SeleniumGrid.class);
    
    /**
     * Constructor for Selenium Grid from hub URL
     * 
     * @param hubUrl {@link URL} for grid hub host
     * @throws IOException if unable to acquire Grid details
     */
    public SeleniumGrid(URL hubUrl) throws IOException {
        hubServer = new GridServer(hubUrl, GridRole.HUB);
        for (String nodeEndpoint : GridUtility.getGridProxies(hubUrl)) {
            URL nodeUrl = new URL(nodeEndpoint + GridServer.HUB_BASE);
            nodeServers.put(nodeEndpoint, new GridServer(nodeUrl, GridRole.NODE));
        }
    }
    
    /**
     * Constructor for Selenium Grid from server objects.
     * 
     * @param hubServer {@link GridServer} object for hub host
     * @param nodeServers array of {@link GridServer} objects for node hosts
     */
    public SeleniumGrid(GridServer hubServer, GridServer... nodeServers) {
        this.hubServer = Objects.requireNonNull(hubServer);
        if (Objects.requireNonNull(nodeServers).length == 0) {
            throw new IllegalArgumentException("[nodeServers] must be non-empty");
        }
        for (GridServer nodeServer : nodeServers) {
            String nodeEndpoint = "http://" + nodeServer.getUrl().getAuthority();
            this.nodeServers.put(nodeEndpoint, nodeServer);
        }
    }
    
    /**
     * Create an object that represents the Selenium Grid with the specified hub endpoint.
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
            return new SeleniumGrid(hubUrl);
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
     * 
     * @param config
     * @param personality
     * @return
     */
    public Capabilities getPersonality(SeleniumConfig config, String personality) {
        return config.getCapabilitiesForName(personality)[0];
    }

    public static class GridServer {
        private GridRole role;
        private URL serverUrl;
        private String statusRequest;
        private String shutdownRequest;
        
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
                    new UrlChecker().waitUntilUnavailable(SHUTDOWN_DELAY, TimeUnit.SECONDS, serverUrl);
                    Thread.sleep(1000);
                } catch (IOException | org.openqa.selenium.net.UrlChecker.TimeoutException e) {
                    throw UncheckedThrow.throwUnchecked(e);
                }
            }
            
            return true;
        }
    }
}
