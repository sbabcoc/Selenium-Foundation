package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.openqa.selenium.net.UrlChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.common.base.UncheckedThrow;

@SuppressWarnings("squid:S1774")
public class SeleniumGrid {
    
    protected static final Logger LOGGER = LoggerFactory.getLogger(SeleniumGrid.class);
    
    /**
     * Private constructor to prevent direct instantiation from outside.
     */
    protected SeleniumGrid() {
    }
    
    /**
     * TODO - Factor out common server properties and behaviors to support remote Grid.
     * Add knowledge of node properties and behaviors. Add shutdown methods.
     */
    public static class GridServer {
        private HttpHost serverHost;    // COMMON
        private String statusRequest;   // COMMON (can be assembled on request)
        private String shutdownRequest; // COMMON (can be assembled on request)
        
        private static final long SHUTDOWN_DELAY = 15;
        private static final String HUB_SHUTDOWN = "/lifecycle-manager?action=shutdown";
        
        public boolean stopGridServer(final boolean localOnly) {
            return stopGridServer(serverHost, statusRequest, shutdownRequest, localOnly);
        }
        
        /**
         * Stop the specified Selenium Grid server.
         * 
         * @param serverParms Selenium Grid server parameters
         * @param localOnly 'true' to target only local Grid server
         * @return 'false' if [localOnly] and server is remote; otherwise 'true'
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
