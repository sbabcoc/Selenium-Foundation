package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openqa.grid.common.GridRole;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.net.UrlChecker;
import org.openqa.selenium.net.UrlChecker.TimeoutException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig;
import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.exceptions.GridServerLaunchFailedException;
import com.nordstrom.automation.selenium.exceptions.InvalidGridHostException;
import com.nordstrom.common.base.UncheckedThrow;

/**
 * This class provides basic support for interacting with a Selenium Grid instance.
 */
public final class GridUtility {
    
    private static final String GRID_ENDPOINT = "/wd/hub/";
    private static final String HUB_STATUS = "/grid/api/hub/";
    private static final String NODE_STATUS = "/wd/hub/status";
    
    private static final long SHUTDOWN_DELAY = 15;
    private static final String HUB_SHUTDOWN = "/lifecycle-manager?action=shutdown";
    
    private static Process hubProcess;
    private static Process nodeProcess;
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
        AbstractSeleniumConfig config = AbstractSeleniumConfig.getConfig();
        boolean isActive = isHubActive(config);
        
        try {
            if (!isActive && isLocalHost(config.getHubAuthority())) {
                startGridServer(GridServerParms.getHubParms(config));
                startGridServer(GridServerParms.getNodeParms(config));
                isActive = true;
            }
        } catch (GridServerLaunchFailedException e) {
            LOGGER.warn("Unable to launch Selenium Grid server", e);
        } catch (TimeoutException e) {
            LOGGER.warn("Timeout waiting for Selenium Grid server to be active", e);
        }
        
        return isActive;
    }
    
    /**
     * Determine if the configured Selenium Grid hub is active.
     * 
     * @param config Selenium configuration object
     * @return 'true' if configured hub is active; otherwise 'false'
     */
    public static boolean isHubActive(AbstractSeleniumConfig config) {
        return isHostActive(config.getHubAuthority(), HUB_STATUS);
    }

    /**
     * Start the specified Selenium Grid server.
     * 
     * @param serverParms Selenium Grid server parameters
     * @throws TimeoutException If Grid server took too long to activate.
     */
    private static void startGridServer(final GridServerParms serverParms) throws TimeoutException {
        if (!isHostActive(serverParms.serverHost, serverParms.statusRequest)) {
            AbstractSeleniumConfig config = AbstractSeleniumConfig.getConfig();
            String launcherClassName = config.getLauncherClassName();
            String[] dependencyContexts = config.getDependencyContexts();
            
            if (serverParms.processRole == GridRole.NODE) {
                String browserName = config.getBrowserName();
                for (DriverPlugin driverPlugin : ServiceLoader.load(DriverPlugin.class)) {
                    if (browserName.equals(driverPlugin.getBrowserName())) {
                        String[] driverContexts = driverPlugin.getDependencyContexts();
                        dependencyContexts = Stream
                                        .concat(Stream.of(dependencyContexts), Stream.of(driverContexts))
                                        .toArray(String[]::new);
                        break;
                    }
                }
            }
            
            Process serverProcess = GridProcess.start(launcherClassName, dependencyContexts, serverParms.processArgs);
            new UrlChecker().waitUntilAvailable(WaitType.HOST.getInterval(), TimeUnit.SECONDS, serverParms.statusUrl);
            setProcess(serverParms.processRole, serverProcess);
        }
    }
    
    /**
     * Store the specified Selenium Grid server process using the specified role.
     * 
     * @param processRole Selenium Grid server role (either HUB or NODE)
     * @param serverProcess Selenium Grid server process
     */
    private static void setProcess(final GridRole processRole, final Process serverProcess) {
        switch (processRole) {
            case HUB:
                setHubProcess(serverProcess);
                break;
            case NODE:
                setNodeProcess(serverProcess);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Determine if the specified Selenium Grid host (hub or node) is active.
     * 
     * @param host HTTP host connection to be checked
     * @param request request path (may include parameters)
     * @return 'true' if specified host is active; otherwise 'false'
     */
    private static boolean isHostActive(final HttpHost host, final String request) {
        try {
            HttpResponse response = getHttpResponse(host, request);
            return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        } catch (IOException e) { //NOSONAR
            return false;
        }
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
        AbstractSeleniumConfig config = AbstractSeleniumConfig.getConfig();
        GridServerParms hubParms = GridServerParms.getHubParms(config);
        if (isHubActive()) {
            return new RemoteWebDriver(hubParms.endpointUrl, config.getBrowserCaps());
        } else {
            throw new IllegalStateException("No Selenium Grid instance was found at " + hubParms.endpointUrl);
        }
    }
    
    /**
     * Stop the configured Selenium Grid node server.
     * 
     * @param localOnly 'true' to target only local Grid node server
     * @return 'false' if [localOnly] and node is remote; otherwise 'true'
     */
    public static boolean stopGridNode(final boolean localOnly) {
        return stopGridServer(GridServerParms.getNodeParms(AbstractSeleniumConfig.getConfig()), localOnly);
    }
    
    /**
     * Stop the configured Selenium Grid hub server.
     * 
     * @param localOnly 'true' to target only local Grid hub server
     * @return 'false' if [localOnly] and hub is remote; otherwise 'true'
     */
    public static boolean stopGridHub(final boolean localOnly) {
        return stopGridServer(GridServerParms.getHubParms(AbstractSeleniumConfig.getConfig()), localOnly);
    }
    
    /**
     * Stop the specified Selenium Grid server.
     * 
     * @param serverParms Selenium Grid server parameters
     * @param localOnly 'true' to target only local Grid server
     * @return 'false' if [localOnly] and server is remote; otherwise 'true'
     */
    public static boolean stopGridServer(final GridServerParms serverParms, final boolean localOnly) {
        if (localOnly && !isLocalHost(serverParms.serverHost)) {
            return false;
        }
        
        if (isHostActive(serverParms.serverHost, serverParms.statusRequest)) {
            try {
                URL hostUrl = URI.create(serverParms.serverHost.toURI()).toURL();
                GridUtility.getHttpResponse(serverParms.serverHost, serverParms.shutdownRequest);
                new UrlChecker().waitUntilUnavailable(SHUTDOWN_DELAY, TimeUnit.SECONDS, hostUrl);
            } catch (IOException | TimeoutException e) {
                throw UncheckedThrow.throwUnchecked(e);
            }
        }
        
        setProcess(serverParms.processRole, null);
        return true;
    }
    
    /**
     * Determine if the configured Selenium Grid node server is the local host.
     * 
     * @return 'true' if Grid node is local host; otherwise 'false'
     */
    public static boolean isLocalNode() {
        return isLocalHost(AbstractSeleniumConfig.getConfig().getNodeAuthority());
    }
    
    /**
     * Determine if the configured Selenium Grid hub server is the local host.
     * 
     * @return 'true' if Grid hub is local host; otherwise 'false'
     */
    public static boolean isLocalHub() {
        return isLocalHost(AbstractSeleniumConfig.getConfig().getHubAuthority());
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
    
    /**
     * This private class encapsulated the parameters for a Selenium Grid server.
     */
    @SuppressWarnings("squid:S2972")
    private static class GridServerParms {
        
        private GridRole processRole;
        private String[] processArgs;
        private HttpHost serverHost;
        private URL endpointUrl;
        private String statusRequest;
        private URL statusUrl;
        private String shutdownRequest;

        /**
         * Assemble parameters object for the configured Selenium Grid hub.
         * 
         * @param config WebDriver/Grid configuration
         * @return Grid hub parameters object
         */
        public static GridServerParms getHubParms(final AbstractSeleniumConfig config) {
            GridServerParms parms = new GridServerParms();
            parms.processRole = GridRole.HUB;
            parms.processArgs = config.getHubArgs();
            parms.serverHost = config.getHubAuthority();
            parms.statusRequest = HUB_STATUS;
            parms.shutdownRequest = HUB_SHUTDOWN;
            
            try {
                parms.endpointUrl = URI.create(parms.serverHost.toURI() + GRID_ENDPOINT).toURL();
                parms.statusUrl = URI.create(parms.serverHost.toURI() + parms.statusRequest).toURL();
            } catch (MalformedURLException e) {
                throw new InvalidGridHostException("hub", parms.serverHost, e);
            }
            
            return parms;
        }
        
        /**
         * Assemble parameters object for the configured Selenium Grid node.
         * 
         * @param config WebDriver/Grid configuration
         * @return Grid node parameters object
         */
        public static GridServerParms getNodeParms(final AbstractSeleniumConfig config) {
            GridServerParms parms = new GridServerParms();
            parms.processRole = GridRole.NODE;
            parms.processArgs = config.getNodeArgs();
            parms.serverHost = config.getNodeAuthority();
            parms.statusRequest = NODE_STATUS;
            parms.shutdownRequest = config.getNodeShutdownRequest();
            
            try {
                parms.endpointUrl = URI.create(parms.serverHost.toURI() + GRID_ENDPOINT).toURL();
                parms.statusUrl = URI.create(parms.serverHost.toURI() + parms.statusRequest).toURL();
            } catch (MalformedURLException e) {
                throw new InvalidGridHostException("node", parms.serverHost, e);
            }
            
            return parms;
        }
    }
    
    /**
     * Set a new Selenium Grid hub process, or stop an existing one.
     * 
     * @param process Selenium Grid server process; 'null' to stop process
     * @return 'true' if existing hub process was stopped; otherwise 'false'
     */
    private static synchronized boolean setHubProcess(final Process process) {
        boolean hasHubProcess = (hubProcess != null);
        if (hasHubProcess) {
            LOGGER.debug("Destroying current Grid hub process {}", hubProcess);
            hubProcess.destroy();
        }
        if (process != null) {
            LOGGER.debug("Setting new Grid hub process {}", process);
        }
        hubProcess = process;
        return hasHubProcess;
    }
    
    /**
     * Set a new Selenium Grid node process, or stop an existing one.
     * 
     * @param process Selenium Grid server process; 'null' to stop process
     * @return 'true' if existing node process was stopped; otherwise 'false'
     */
    private static synchronized boolean setNodeProcess(final Process process) {
        boolean hasNodeProcess = (nodeProcess != null);
        if (hasNodeProcess) {
            LOGGER.debug("Destroying current Grid node process {}", nodeProcess);
            nodeProcess.destroy();
        }
        if (process != null) {
            LOGGER.debug("Setting new Grid node process {}", process);
        }
        nodeProcess = process;
        return hasNodeProcess;
    }
}
