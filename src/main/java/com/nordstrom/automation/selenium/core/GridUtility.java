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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.net.UrlChecker;
import org.openqa.selenium.net.UrlChecker.TimeoutException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.SeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.exceptions.GridServerLaunchFailedException;
import com.nordstrom.automation.selenium.exceptions.InvalidGridHostException;
import com.nordstrom.automation.selenium.exceptions.UnknownGridHostException;

/**
 * This class provides basic support for interacting with a Selenium Grid instance.
 */
public final class GridUtility {
    
    private static final String GRID_HUB = "GridHub";
    private static final String GRID_NODE = "GridNode";
    private static final String GRID_ENDPOINT = "/wd/hub/";
    private static final String HUB_STATUS = "/grid/api/hub/";
    private static final String NODE_STATUS = "/wd/hub/status/";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JsUtility.class);
    
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
        return isHubActive(Reporter.getCurrentTestResult());
    }
    
    /**
     * Determine if the configured Selenium Grid hub is active.<br>
     * <b>NOTE</b>: If configured for local execution, this method ensures that a local hub and node are active.
     * 
     * @param testResult configuration context (TestNG test result object)
     * @return 'true' if configured hub is active; otherwise 'false'
     */
    public static boolean isHubActive(ITestResult testResult) {
        
        Objects.requireNonNull(testResult, "Test result object must be non-null");
        
        SeleniumConfig config = SeleniumConfig.getConfig(testResult);
        GridHubConfiguration hubConfig = config.getHubConfig();
        
        boolean isActive = isHubActive(hubConfig);
        
        try {
            if (!isActive && isThisMyIpAddress(InetAddress.getByName(hubConfig.getHost()))) {
                startGridServer(testResult, GridServerParms.getHubParms(config));
                startGridServer(testResult, GridServerParms.getNodeParms(config));
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
     * 
     * @param testResult configuration context (TestNG test result object)
     * @param serverParms Selenium Grid server parameters
     * @throws TimeoutException If Grid server took too long to activate.
     */
    private static void startGridServer(ITestResult testResult, GridServerParms serverParms) throws TimeoutException {
        Process serverProcess = GridProcess.start(testResult, serverParms.processArgs);
        new UrlChecker().waitUntilAvailable(WaitType.HOST.getInterval(), TimeUnit.SECONDS, serverParms.statusUrl);
        testResult.getTestContext().setAttribute(serverParms.propertyKey, serverProcess);
    }

    /**
     * Determine if the configured Selenium Grid hub is active.
     * 
     * @param hubConfig hub configuration object
     * @return 'true' if configured hub is active; otherwise 'false'
     */
    static boolean isHubActive(GridHubConfiguration hubConfig) {
        return isHostActive(getHubHost(hubConfig), HUB_STATUS);
    }
    
    /**
     * Get an {@link HttpHost} object for the configured Selenium Grid hub.
     * 
     * @param hubConfig hub configuration object
     * @return HttpHost object for configured hub
     */
    static HttpHost getHubHost(GridHubConfiguration hubConfig) {
        return new HttpHost(hubConfig.getHost(), hubConfig.getPort());
    }

    /**
     * Determine if the configured Selenium Grid node is active.
     * 
     * @param nodeConfig node configuration object
     * @return 'true' if configured node is active; otherwise 'false'
     */
    static boolean isNodeActive(RegistrationRequest nodeConfig) {
        return isHostActive(getNodeHost(nodeConfig), NODE_STATUS);
    }
    
    /**
     * Get an {@link HttpHost} object for the configured Selenium Grid node.
     * 
     * @param nodeConfig node configuration object
     * @return HttpHost object for configured node
     */
    static HttpHost getNodeHost(RegistrationRequest nodeConfig) {
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
    private static boolean isHostActive(HttpHost host, String request) {
        try {
            HttpResponse response = getHttpResponse(host, request);
            return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        } catch (IOException e) {
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
    public static HttpResponse getHttpResponse(HttpHost host, String request) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        URL sessionURL = new URL(host.toURI() + request);
        BasicHttpEntityEnclosingRequest basicHttpEntityEnclosingRequest = 
                new BasicHttpEntityEnclosingRequest("GET", sessionURL.toExternalForm());
        return client.execute(host, basicHttpEntityEnclosingRequest);
    }
    
    /**
     * Get the Selenium driver for the current configuration context.
     * 
     * @return driver object (may be 'null')
     */
    public static WebDriver getDriver() {
        return getDriver(Reporter.getCurrentTestResult());
    }
    
    /**
     * Get the Selenium driver for the specified configuration context.
     * 
     * @param testResult configuration context (TestNG test result object)
     * @return driver object (may be 'null')
     */
    public static WebDriver getDriver(ITestResult testResult) {
        Objects.requireNonNull(testResult, "Test result object must be non-null");
        
        SeleniumConfig config = SeleniumConfig.getConfig(testResult);
        GridServerParms hubParms = GridServerParms.getHubParms(config);
        if (isHubActive(testResult)) {
            return new RemoteWebDriver(hubParms.endpointUrl, config.getBrowserCaps());
        } else {
            throw new IllegalStateException("No Selenium Grid instance was found at " + hubParms.endpointUrl);
        }
    }
    
    /**
     * Get the Selenium Grid hub server process for the current configuration context.
     * 
     * @return process object for the hub (may be 'null')
     */
    public static Process getGridHub() {
        return getGridHub(Reporter.getCurrentTestResult().getTestContext());
    }
    
    /**
     * Get the Selenium Grid hub server process for the specified configuration context.
     * 
     * @param testContext configuration context (TestNG test context object)
     * @return process object for the hub (may be 'null')
     */
    public static Process getGridHub(ITestContext testContext) {
        return (Process) testContext.getAttribute(GRID_HUB);
    }

    /**
     * Get the Selenium Grid node server process for the current configuration context.
     * 
     * @return process object for the node (may be 'null')
     */
    public static Process getGridNode() {
        return getGridNode(Reporter.getCurrentTestResult().getTestContext());
    }
    
    /**
     * Get the Selenium Grid node server process for the specified configuration context.
     * 
     * @param testContext configuration context (TestNG test context object)
     * @return process object for the node (may be 'null')
     */
    public static Process getGridNode(ITestContext testContext) {
        return (Process) testContext.getAttribute(GRID_NODE);
    }

    /**
     * Determine if the specified address is local to the machine we're running on.
     * 
     * @param addr Internet protocol address object
     * @return 'true' if the specified address is local; otherwise 'false'
     */
    public static boolean isThisMyIpAddress(InetAddress addr) {
        // Check if the address is a valid special local or loop back
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
            return true;
        }

        // Check if the address is defined on any interface
        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (SocketException e) {
            LOGGER.warn("Attempt to associate IP address with adapter triggered I/O exception: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * This private class encapsulated the parameters for a Selenium Grid server.
     */
    private static class GridServerParms {
        
        private String propertyKey;
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
        public static GridServerParms getHubParms(SeleniumConfig config) {
            GridServerParms parms = new GridServerParms();
            parms.propertyKey = GRID_HUB;
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
        public static GridServerParms getNodeParms(SeleniumConfig config) {
            GridServerParms parms = new GridServerParms();
            parms.propertyKey = GRID_NODE;
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
}
