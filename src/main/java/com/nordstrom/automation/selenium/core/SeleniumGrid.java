package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.openqa.grid.common.GridRole;
import org.openqa.selenium.net.UrlChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.common.base.UncheckedThrow;

@SuppressWarnings("squid:S1774")
public class SeleniumGrid {
    
    protected GridServer hubServer;
    protected List<? extends GridServer> nodeServers = new ArrayList<>();

    protected static final Logger LOGGER = LoggerFactory.getLogger(SeleniumGrid.class);
    
    public <T extends GridServer> SeleniumGrid(T hubServer, List<T> nodeServers) {
        this.hubServer = Objects.requireNonNull(hubServer);
        this.nodeServers = Objects.requireNonNull(nodeServers);
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
    public List<? extends GridServer> getNodeServers() {
        return nodeServers;
    }
    
    public static class GridServer {
        private boolean isHub;
        private HttpHost serverHost;
        private String statusRequest;
        private String shutdownRequest;
        
        public static final String GRID_CONSOLE = "/grid/console";
        public static final String NODE_STATUS = "/wd/hub/status";
        public static final String HUB_CONFIG = "/grid/api/hub/";
        public static final String NODE_CONFIG = "/grid/api/proxy";
        
        private static final String HUB_SHUTDOWN = "/lifecycle-manager?action=shutdown";
        private static final String NODE_SHUTDOWN = "/extra/LifecycleServlet?action=shutdown";
        private static final long SHUTDOWN_DELAY = 15;
        
        public GridServer(HttpHost host, GridRole role) {
            this.serverHost = host;
            if (role == GridRole.HUB) {
                isHub = true;
                statusRequest = HUB_CONFIG;
                shutdownRequest = HUB_SHUTDOWN;
            } else {
                isHub = false;
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
            return isHub;
        }
        
        /**
         * Get the HTTP host for this server.
         * 
         * @return {@link HttpHost} object for this server
         */
        public HttpHost getHost() {
            return serverHost;
        }
        
        /**
         * Stop this Selenium Grid server.
         * 
         * @param localOnly {@code true} to target only local Grid server
         * @return {@code false} if [localOnly] and server is remote; otherwise {@code true}
         */
        public boolean stopGridServer(final boolean localOnly) {
            return stopGridServer(serverHost, statusRequest, shutdownRequest, localOnly);
        }
        
        /**
         * Stop the specified Selenium Grid server.
         * 
         * @param serverParms Selenium Grid server parameters
         * @param localOnly {@code true} to target only local Grid server
         * @return {@code false} if [localOnly] and server is remote; otherwise {@code true}
         */
        public static boolean stopGridServer(final HttpHost serverHost, final String statusRequest,
                        final String shutdownRequest, final boolean localOnly) {
            
            if (localOnly && !GridUtility.isLocalHost(serverHost)) {
                return false;
            }
            
            if (GridUtility.isHostActive(serverHost, statusRequest)) {
                try {
                    URL hostUrl = URI.create(serverHost.toURI()).toURL();
                    GridUtility.getHttpResponse(serverHost, shutdownRequest);
                    new UrlChecker().waitUntilUnavailable(SHUTDOWN_DELAY, TimeUnit.SECONDS, hostUrl);
                } catch (IOException | org.openqa.selenium.net.UrlChecker.TimeoutException e) {
                    throw UncheckedThrow.throwUnchecked(e);
                }
            }
            
            return true;
        }
    }

}
