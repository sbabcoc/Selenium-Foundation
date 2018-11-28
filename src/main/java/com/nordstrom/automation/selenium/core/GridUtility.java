package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Paths;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openqa.grid.common.GridRole;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.net.UrlChecker.TimeoutException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.exceptions.GridServerLaunchFailedException;

/**
 * This class provides basic support for interacting with a Selenium Grid instance.
 */
public final class GridUtility {
    
    private static LocalGrid localGrid;
    
    private static final String NODE_STATUS = "/wd/hub/status";
    private static final String HUB_CONFIG = "/grid/api/hub/";
    private static final String NODE_CONFIG = "/grid/api/proxy";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GridUtility.class);
    
    /**
     * Private constructor to prevent instantiation.
     */
    private GridUtility() {
        throw new AssertionError("GridUtility is a static utility class that cannot be instantiated");
    }
    
    /**
     * Determine if the configured Selenium Grid hub is active.<br>
     * <b>NOTE</b>: If configured for local execution, this method ensures that a local hub and node are active.
     * 
     * @return 'true' if configured hub is active; otherwise 'false'
     */
    public static boolean isHubActive() {
        SeleniumConfig config = AbstractSeleniumConfig.getConfig();
        HttpHost host = config.getHubAuthority();
        boolean isActive = isHubActive(host);
        
        if (!isActive && ((host == null) || isLocalHost(host))) {
            try {
                localGrid = LocalGrid.launch(config, Paths.get(config.getHubConfigPath()));
                isActive = true;
            } catch (GridServerLaunchFailedException | IOException e) {
                LOGGER.warn("Unable to launch Selenium Grid server", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        return isActive;
    }
    
    /**
     * Determine if the configured Selenium Grid hub is active.
     * 
     * @param host HTTP host connection to be checked (may be {@code null})
     * @return 'true' if configured hub is active; otherwise 'false'
     */
    public static boolean isHubActive(HttpHost host) {
        return isHostActive(host, HUB_CONFIG);
    }

    /**
     * Determine if the specified Selenium Grid host (hub or node) is active.
     * 
     * @param host HTTP host connection to be checked (may be {@code null})
     * @param request request path (may include parameters)
     * @return 'true' if specified host is active; otherwise 'false'
     */
    public static boolean isHostActive(final HttpHost host, final String request) {
        if (host != null) {
            try {
                HttpResponse response = getHttpResponse(host, request);
                return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
            } catch (IOException e) { //NOSONAR
                // nothing to do here
            }
        }
        return false;
    }
    
    /**
     * Send the specified GET request to the indicated host.
     * 
     * @param host target HTTP host connection
     * @param request request path (may include parameters)
     * @return host response for the specified GET request
     * @throws IOException The request triggered an I/O exception
     */
    public static HttpResponse getHttpResponse(final HttpHost host, final String request) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        URL sessionURL = new URL(host.toURI() + request);
        BasicHttpEntityEnclosingRequest basicHttpEntityEnclosingRequest = 
                new BasicHttpEntityEnclosingRequest("GET", sessionURL.toExternalForm());
        return client.execute(host, basicHttpEntityEnclosingRequest);
    }
    
    /**
     * Get the Selenium driver for the specified test class instance.
     * 
     * @return driver object (may be 'null')
     */
    public static WebDriver getDriver() {
        SeleniumConfig config = AbstractSeleniumConfig.getConfig();
        
        try {
            LocalGrid.launch(config, Paths.get(config.getHubConfigPath()));
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
//        
//        SeleniumConfig config = AbstractSeleniumConfig.getConfig();
//        GridServerParms hubParms = GridServerParms.getHubParms(config);
//        if (isHubActive()) {
//            return new RemoteWebDriver(hubParms.endpointUrl, config.getCurrentCapabilities());
//        } else {
//            throw new IllegalStateException("No Selenium Grid instance was found at " + hubParms.endpointUrl);
//        }
    }
    
    /**
     * FIXME
     * @param role
     * @return
     */
    public String getStatusPath(GridRole role) {
        if (role == GridRole.HUB) {
            return HUB_CONFIG;
        } else if (role == GridRole.NODE) {
            return NODE_STATUS;
        }
        throw new IllegalArgumentException("Specified [role] is unsupported: " + role);
    }
    
    /**
     * Determine if the specified server is the local host.
     * 
     * @param host HTTP host connection to be checked
     * @return 'true' if server is local host; otherwise 'false'
     */
    public static boolean isLocalHost(HttpHost host) {
        try {
            InetAddress addr = InetAddress.getByName(host.getHostName());
            return (GridUtility.isThisMyIpAddress(addr));
        } catch (UnknownHostException e) {
            LOGGER.warn("Unable to get IP address for '{}'", host.getHostName(), e);
            return false;
        }
    }
    
    /**
     * Determine if the specified address is local to the machine we're running on.
     * 
     * @param addr Internet protocol address object
     * @return 'true' if the specified address is local; otherwise 'false'
     */
    public static boolean isThisMyIpAddress(final InetAddress addr) {
        // Check if the address is a valid special local or loop back
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
            return true;
        }

        // Check if the address is defined on any interface
        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (SocketException e) { //NOSONAR
            LOGGER.warn("Attempt to associate IP address with adapter triggered I/O exception: {}", e.getMessage());
            return false;
        }
    }
}
