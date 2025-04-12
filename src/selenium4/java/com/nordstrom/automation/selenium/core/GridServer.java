package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.grid.data.Availability;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.JsonInput;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.plugins.AbstractAppiumPlugin.AppiumGridServer;

/**
 * This class represents a single Selenium Grid server (hub or node).
 */
public class GridServer {
    private boolean isHub;
    private URL serverUrl;
    
    /** base path for hub server URLs */
    public static final String HUB_BASE = "/wd/hub";
    /** sub-path for Grid server 'status' endpoint */
    public static final String SERVER_STATUS = "/status";
    /** sub-path for Grid server 'register' endpoint */
    public static final String GRID_REGISTER = "/grid/register";
    
    /**
     * Constructor for Grid server object.
     * 
     * @param url base {@link URL} for Grid server
     * @param isHub role of Grid server being started ({@code true} = hub; {@code false} = node)
     */
    public GridServer(URL url, boolean isHub) {
        this.isHub = isHub;
        this.serverUrl = url;
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
     * Get the URL for this server.
     * 
     * @return {@link URL} object for this server
     */
    public URL getUrl() {
        return serverUrl;
    }
    
    /**
     * Determine if this server is active.
     * 
     * @return {@code true} if server is active; otherwise {@code false}
     */
    public boolean isActive() {
        return GridUtility.isHostActive(serverUrl, SERVER_STATUS);
    }
    
    /**
     * Stop the Selenium Grid server represented by this object.
     * 
     * @param localOnly {@code true} to target only local Grid server
     * @return {@code false} if [localOnly] and server is remote; otherwise {@code true}
     * @throws InterruptedException if this thread was interrupted
     */
    public boolean shutdown(final boolean localOnly) throws InterruptedException {
        boolean isLocal = GridUtility.isLocalHost(serverUrl);
        if (localOnly && !isLocal) {
            return false;
        }
        
        if (isActive()) {
            if (isLocal) {
                if (!isHub() && AppiumGridServer.shutdownAppiumWithPM2(serverUrl)) {
                    return true;
                }

// FIXME: This functionality relies on Selenium 3 Grid server remote shutdown features that are no
// longer available in Selenium 4. New implementation will require determination of the server PID
// and command line process termination.

//              try {
//                  GridUtility.getHttpResponse(serverUrl, shutdownRequest);
//                  SeleniumGrid.waitUntilUnavailable(SHUTDOWN_DELAY, TimeUnit.SECONDS, serverUrl);
//                  Thread.sleep(1000);
//              } catch (IOException | org.openqa.selenium.net.UrlChecker.TimeoutException e) {
//                  throw UncheckedThrow.throwUnchecked(e);
//              }
                
                /*
                 * FIXME: Implement this -
                 * 
                 * Get process IDs associated with port
                 * - on Windows: netstat -a -n -o | find "123456"
                 * - on Mac: netstat -vanp tcp | grep 123456
                 * - on Linux: netstat -ltnp | grep -w ':123456'
                 * 
                 * Can I create a process object using a found PID? Doesn't seem that I can.
                */
            }
        }
        
        return true;
    }

    /**
     * Determine if the specified Selenium Grid hub is active.
     * 
     * @param hubUrl {@link URL} to be checked
     * @return 'true' if specified hub is active; otherwise 'false'
     */
    public static boolean isHubActive(URL hubUrl) {
        return GridUtility.isHostActive(hubUrl, SERVER_STATUS);
    }
    
    /**
     * Determine if the indicated Selenium Grid node is registered with the specified hub.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubUrl {@link URL} of hub to query
     * @param nodeUrl {@link URL} of node in question
     * @return 'true' if indicated node is registered; otherwise 'false'
     */
    public static boolean isNodeRegistered(SeleniumConfig config, URL hubUrl, URL nodeUrl) {
        try {
            List<NodeStatus> nodes = getStatusOfNodes(config, hubUrl);
            URI nodeUri = URI.create(nodeUrl.getProtocol() + "://" + nodeUrl.getAuthority());
            return nodes.stream()
                    .filter(node -> node.getUri().equals(nodeUri) && node.getStatus().equals(Availability.UP))
                    .findFirst().isPresent();
        } catch (NullPointerException | ClassCastException eaten) {
            // nothing to do here
        }
        return false;
    }

    /**
     * Get the list of node endpoints attached to the specified Selenium Grid hub.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubUrl {@link URL} of Grid hub
     * @return list of node endpoints
     * @throws IOException if an I/O error occurs
     */
    public static List<URL> getGridProxies(SeleniumConfig config, URL hubUrl) throws IOException {
        List<URL> nodeList = new ArrayList<>();
        List<NodeStatus> nodes = getStatusOfNodes(config, hubUrl);
        for (NodeStatus node : nodes) {
            nodeList.add(node.getUri().toURL());
        }
        return nodeList;
    }
    
    /**
     * Get capabilities of the indicated node of the specified Selenium Grid hub.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubUrl {@link URL} of Grid hub
     * @param nodeUrl {@link URL} of Grid node
     * @return list of {@link Capabilities} objects for the specified node
     * @throws IOException if an I/O error occurs
     */
    public static List<Capabilities> getNodeCapabilities(SeleniumConfig config, URL hubUrl, URL nodeUrl) throws IOException {
        URI nodeUri = URI.create(nodeUrl.getProtocol() + "://" + nodeUrl.getAuthority());
        List<NodeStatus> nodes = getStatusOfNodes(config, hubUrl);
        return nodes.stream().filter(node -> node.getUri().equals(nodeUri))
                .map(node -> node.getCapabilities()).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private static List<NodeStatus> getStatusOfNodes(SeleniumConfig config, URL hubUrl) {
        try {
            HttpResponse response = GridUtility.callGraphQLService(hubUrl, Nodes.NODE_STATUS);
            String json = EntityUtils.toString(response.getEntity());
            JsonInput input = new Json().newInput(new StringReader(json));
            return Nodes.fromJson(input);
        } catch (IOException eaten) {
            return Collections.emptyList();
        }
    }
}

