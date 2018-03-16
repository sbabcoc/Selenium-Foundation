package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openqa.grid.common.GridRole;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.net.UrlChecker;
import org.openqa.selenium.net.UrlChecker.TimeoutException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.SeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.exceptions.GridServerLaunchFailedException;
import com.nordstrom.automation.selenium.exceptions.InvalidGridHostException;
import com.nordstrom.automation.selenium.exceptions.UnknownGridHostException;
import com.nordstrom.common.base.UncheckedThrow;

/**
 * This class provides basic support for interacting with a Selenium Grid instance.
 */
public final class GridUtility {
    
    private static final String GRID_ENDPOINT = "/wd/hub/";
    private static final String HUB_STATUS = "/grid/api/hub/";
    private static final String NODE_STATUS = "/wd/hub/status/";
    
    private static final long SHUTDOWN_DELAY = 15;
    private static final String HUB_SHUTDOWN = "/lifecycle-manager?action=shutdown";
    private static final String NODE_SHUTDOWN = "/selenium-server/driver/?cmd=shutDownSeleniumServer";
    
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
        SeleniumConfig config = SeleniumConfig.getConfig();
        GridHubConfiguration hubConfig = config.getHubConfig();
        
        boolean isActive = isHubActive(hubConfig);
        
        try {
            if (!isActive && isThisMyIpAddress(InetAddress.getByName(hubConfig.getHost()))) {
                startGridServer(GridServerParms.getHubParms(config));
                startGridServer(GridServerParms.getNodeParms(config));
                isActive = true;
            }
        } catch (UnknownHostException e) {
            throw new UnknownGridHostException("hub", hubConfig.getHost(), e);
        } catch (GridServerLaunchFailedException e) {
            LOGGER.warn("Unable to launch Selenium Grid server", e);
        } catch (TimeoutException e) {
            LOGGER.warn("Timeout waiting for Selenium Grid server to be active", e);
        }
        
        return isActive;
    }

    /**
     * Start the specified Selenium Grid server.
     * @param serverParms Selenium Grid server parameters
     * 
     * @throws TimeoutException If Grid server took too long to activate.
     */
    private static void startGridServer(final GridServerParms serverParms) throws TimeoutException {
        Process serverProcess = GridProcess.start(serverParms.processArgs);
        new UrlChecker().waitUntilAvailable(WaitType.HOST.getInterval(), TimeUnit.SECONDS, serverParms.statusUrl);
        setProcess(serverParms.processRole, serverProcess);
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
     * Determine if the configured Selenium Grid hub is active.
     * 
     * @param hubConfig hub configuration object
     * @return 'true' if configured hub is active; otherwise 'false'
     */
    static boolean isHubActive(final GridHubConfiguration hubConfig) {
        return isHostActive(getHubHost(hubConfig), HUB_STATUS);
    }
    
    /**
     * Get an {@link HttpHost} object for the configured Selenium Grid hub.
     * 
     * @param hubConfig hub configuration object
     * @return HttpHost object for configured hub
     */
    static HttpHost getHubHost(final GridHubConfiguration hubConfig) {
        return new HttpHost(hubConfig.getHost(), hubConfig.getPort());
    }

    /**
     * Determine if the configured Selenium Grid node is active.
     * 
     * @param nodeConfig node configuration object
     * @return 'true' if configured node is active; otherwise 'false'
     */
    static boolean isNodeActive(final RegistrationRequest nodeConfig) {
        return isHostActive(getNodeHost(nodeConfig), NODE_STATUS);
    }
    
    /**
     * Get an {@link HttpHost} object for the configured Selenium Grid node.
     * 
     * @param nodeConfig node configuration object
     * @return HttpHost object for configured node
     */
    static HttpHost getNodeHost(final RegistrationRequest nodeConfig) {
        Map<String, Object> config = nodeConfig.getConfiguration();
        return new HttpHost((String) config.get("host"), (Integer) config.get("port"));
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
        SeleniumConfig config = SeleniumConfig.getConfig();
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
        if (localOnly && !isLocalNode()) {
            return false;
        }
        
        RegistrationRequest nodeConfig = SeleniumConfig.getConfig().getNodeConfig();
        if (isNodeActive(nodeConfig)) {
            HttpHost nodeHost = GridUtility.getNodeHost(nodeConfig);
            try {
                URL nodeUrl = URI.create(nodeHost.toURI()).toURL();
                GridUtility.getHttpResponse(nodeHost, NODE_SHUTDOWN);
                new UrlChecker().waitUntilUnavailable(SHUTDOWN_DELAY, TimeUnit.SECONDS, nodeUrl);
            } catch (IOException | TimeoutException e) {
                throw UncheckedThrow.throwUnchecked(e);
            }
        }
        
        setNodeProcess(null);
        return true;
    }
    
    /**
     * Stop the configured Selenium Grid hub server.
     * 
     * @param localOnly 'true' to target only local Grid hub server
     * @return 'false' if [localOnly] and hub is remote; otherwise 'true'
     */
    public static boolean stopGridHub(final boolean localOnly) {
        if (localOnly && !isLocalHub()) {
            return false;
        }
        
        GridHubConfiguration hubConfig = SeleniumConfig.getConfig().getHubConfig();
        if (isHubActive(hubConfig)) {
            HttpHost hubHost = GridUtility.getHubHost(hubConfig);
            try {
                URL hubUrl = URI.create(hubHost.toURI()).toURL();
                GridUtility.getHttpResponse(hubHost, HUB_SHUTDOWN);
                new UrlChecker().waitUntilUnavailable(SHUTDOWN_DELAY, TimeUnit.SECONDS, hubUrl);
            } catch (IOException | TimeoutException e) {
                throw UncheckedThrow.throwUnchecked(e);
            }
        }
        
        setHubProcess(null);
        return true;
    }
    
    /**
     * Determine if the configured Selenium Grid node server is the local host.
     * 
     * @return 'true' if Grid node is local host; otherwise 'false'
     */
    public static boolean isLocalNode() {
        RegistrationRequest nodeConfig = SeleniumConfig.getConfig().getNodeConfig();
        HttpHost nodeHost = GridUtility.getNodeHost(nodeConfig);
        try {
            InetAddress nodeAddr = InetAddress.getByName(nodeHost.getHostName());
            return (GridUtility.isThisMyIpAddress(nodeAddr));
        } catch (UnknownHostException e) {
            LOGGER.warn("Unable to get IP address for '{}'", nodeHost.getHostName(), e);
            return false;
        }
    }
    
    /**
     * Determine if the configured Selenium Grid hub server is the local host.
     * 
     * @return 'true' if Grid hub is local host; otherwise 'false'
     */
    public static boolean isLocalHub() {
        GridHubConfiguration hubConfig = SeleniumConfig.getConfig().getHubConfig();
        HttpHost hubHost = GridUtility.getHubHost(hubConfig);
        try {
            InetAddress hubAddr = InetAddress.getByName(hubHost.getHostName());
            return (GridUtility.isThisMyIpAddress(hubAddr));
        } catch (UnknownHostException e) {
            LOGGER.warn("Unable to get IP address for '{}'", hubHost.getHostName(), e);
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
        private URL statusUrl;

        /**
         * Assemble parameters object for the configured Selenium Grid hub.
         * 
         * @param config WebDriver/Grid configuration
         * @return Grid hub parameters object
         */
        public static GridServerParms getHubParms(final SeleniumConfig config) {
            GridServerParms parms = new GridServerParms();
            parms.processRole = GridRole.HUB;
            parms.processArgs = config.getHubArgs();
            parms.serverHost = GridUtility.getHubHost(config.getHubConfig());
            
            try {
                parms.endpointUrl = URI.create(parms.serverHost.toURI() + GRID_ENDPOINT).toURL();
                parms.statusUrl = URI.create(parms.serverHost.toURI() + HUB_STATUS).toURL();
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
        public static GridServerParms getNodeParms(final SeleniumConfig config) {
            GridServerParms parms = new GridServerParms();
            parms.processRole = GridRole.NODE;
            parms.processArgs = config.getNodeArgs();
            parms.serverHost = GridUtility.getNodeHost(config.getNodeConfig());
            
            try {
                parms.endpointUrl = URI.create(parms.serverHost.toURI() + GRID_ENDPOINT).toURL();
                parms.statusUrl = URI.create(parms.serverHost.toURI() + NODE_STATUS).toURL();
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
