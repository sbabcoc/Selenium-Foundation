package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openqa.grid.common.GridRole;
import org.openqa.selenium.net.UrlChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.common.base.UncheckedThrow;

@SuppressWarnings("squid:S1774")
public class SeleniumGrid {
    
    private GridServer hubServer;
    private List<GridServer> nodeServers = new ArrayList<>();

    protected static final Logger LOGGER = LoggerFactory.getLogger(SeleniumGrid.class);
    
    public SeleniumGrid(URL hubUrl) throws IOException {
        hubServer = new GridServer(hubUrl, GridRole.HUB);
        for (String nodeEndpoint : GridUtility.getGridProxies(hubUrl)) {
            URL nodeUrl = new URL(nodeEndpoint + GridServer.HUB_BASE);
            nodeServers.add(new GridServer(nodeUrl, GridRole.NODE));
        }
    }
    
    public SeleniumGrid(GridServer hubServer, List<GridServer> nodeServers) {
        this.hubServer = Objects.requireNonNull(hubServer);
        this.nodeServers = Objects.requireNonNull(nodeServers);
    }
    
    /**
     * 
     * @param config TODO
     * @param hubUrl
     * @return
     * @throws IOException
     * @throws TimeoutException 
     * @throws InterruptedException 
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
        throw new IllegalStateException("Specified remote hub url '" + hubUrl + "' isn't active");
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
     * Get the list of grid server objects for the attached nodes.
     * 
     * @return list of {@link GridServer} objects that represent the attached node servers
     */
    public List<GridServer> getNodeServers() {
        return nodeServers;
    }
    
    public static class GridServer {
        private GridRole role;
        private URL serverUrl;
        
        public static final String GRID_CONSOLE = "/grid/console";
        public static final String HUB_BASE = "/wd/hub";
        public static final String NODE_STATUS = "/wd/hub/status";
        public static final String HUB_CONFIG = "/grid/api/hub/";
        public static final String NODE_CONFIG = "/grid/api/proxy";
        
        private static final long SHUTDOWN_DELAY = 15;
        
        public GridServer(URL url, GridRole role) {
            this.role = role;
            this.serverUrl = url;
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
         * Stop the specified Selenium Grid server.
         * 
         * @param serverUrl Selenium server URL
         * @param serverParms Selenium Grid server parameters
         * @param localOnly {@code true} to target only local Grid server
         * @return {@code false} if [localOnly] and server is remote; otherwise {@code true}
         */
        public static boolean stopGridServer(final URL serverUrl, final String statusRequest,
                        final String shutdownRequest, final boolean localOnly) {
            
            if (localOnly && !GridUtility.isLocalHost(serverUrl)) {
                return false;
            }
            
            if (GridUtility.isHostActive(serverUrl, statusRequest)) {
                try {
                    GridUtility.getHttpResponse(serverUrl, shutdownRequest);
                    new UrlChecker().waitUntilUnavailable(SHUTDOWN_DELAY, TimeUnit.SECONDS, serverUrl);
                } catch (IOException | org.openqa.selenium.net.UrlChecker.TimeoutException e) {
                    throw UncheckedThrow.throwUnchecked(e);
                }
            }
            
            return true;
        }
    }

}
