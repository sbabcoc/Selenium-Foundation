package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.net.UrlChecker.TimeoutException;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.api.GridProxyResponse;
import com.nordstrom.automation.selenium.plugins.AbstractAppiumPlugin.AppiumGridServer;
import com.nordstrom.common.base.UncheckedThrow;

/**
 * This class represents a single Selenium Grid server (hub or node).
 */
public class GridServer {
    private boolean isHub;
    private URL serverUrl;
    protected String statusRequest;
    protected String[] shutdownRequest;
    
    public static final String GRID_CONSOLE = "/grid/console";
    public static final String HUB_BASE = "/wd/hub";
    public static final String NODE_STATUS = "/wd/hub/status";
    public static final String HUB_CONFIG = "/grid/api/hub/";
    public static final String NODE_CONFIG = "/grid/api/proxy";
    
    private static final String[] HUB_SHUTDOWN = { "/lifecycle-manager", "action=shutdown" };
    private static final String[] NODE_SHUTDOWN = { "/extra/LifecycleServlet", "action=shutdown" };
    private static final long SHUTDOWN_DELAY = 15;
    
    /**
     * Constructor for Grid server object.
     * 
     * @param url base {@link URL} for Grid server
     * @param isHub role of Grid server being started ({@code true} = hub; {@code false} = node)
     */
    public GridServer(URL url, boolean isHub) {
        this.isHub = isHub;
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
        return GridUtility.isHostActive(serverUrl, statusRequest);
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
            if (isLocal && !isHub()) {
                if (AppiumGridServer.shutdownAppiumWithPM2(serverUrl)) {
                    return true;
                }
            }
            try {
                GridUtility.getHttpResponse(serverUrl, shutdownRequest);
                SeleniumGrid.waitUntilUnavailable(SHUTDOWN_DELAY, TimeUnit.SECONDS, serverUrl);
                Thread.sleep(1000);
            } catch (IOException | TimeoutException e) {
                throw UncheckedThrow.throwUnchecked(e);
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
        return GridUtility.isHostActive(hubUrl, HUB_CONFIG);
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
            String status = getStatusOfNode(config, hubUrl, nodeUrl);
            Map<String, ?> statusMap = new Json().toType(status, HashMap.class);
            return Boolean.TRUE == statusMap.get("success");
        } catch (IOException eaten) {
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
        String url = hubUrl.getProtocol() + "://" + hubUrl.getAuthority() + GRID_CONSOLE;
        Document doc = Jsoup.connect(url).get();
        Elements proxyIds = doc.select("p.proxyid");
        List<URL> nodeList = new ArrayList<>();
        for (Element proxyId : proxyIds) {
            String text = proxyId.text();
            int beginIndex = text.indexOf("http");
            int endIndex = text.indexOf(',');
            URL nodeUrl = URI.create(text.substring(beginIndex, endIndex)).toURL();
            nodeList.add(nodeUrl);
        }
        return nodeList;
    }
    
    /**
     * Get capabilities of the indicated node of the specified Selenium Grid hub.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubUrl {@link URL} of Grid hub
     * @param nodeUrl {@link URL} of Grid node
     * @return {@link Capabilities} object for the specified node
     * @throws IOException if an I/O error occurs
     */
    public static List<Capabilities> getNodeCapabilities(SeleniumConfig config, URL hubUrl, URL nodeUrl) throws IOException {
        String status = getStatusOfNode(config, hubUrl, nodeUrl);
        return new Json().toType(status, GridProxyResponse.class);
    }
    
    private static String getStatusOfNode(SeleniumConfig config, URL hubUrl, URL nodeUrl) throws IOException {
        String nodeEndpoint = nodeUrl.getProtocol() + "://" + nodeUrl.getAuthority();
        String url = hubUrl.getProtocol() + "://" + hubUrl.getAuthority() + NODE_CONFIG + "?id=" + nodeEndpoint;
        try (InputStream is = URI.create(url).toURL().openStream()) {
            return GridUtility.readAvailable(is);
        }
    }
    
}
