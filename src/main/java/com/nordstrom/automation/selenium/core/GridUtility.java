package com.nordstrom.automation.selenium.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.SeleniumGrid.GridServer;
import com.nordstrom.common.base.UncheckedThrow;

/**
 * This class provides basic support for interacting with a Selenium Grid instance.
 */
public final class GridUtility {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GridUtility.class);
    
    /**
     * Private constructor to prevent instantiation.
     */
    private GridUtility() {
        throw new AssertionError("GridUtility is a static utility class that cannot be instantiated");
    }
    
    /**
     * Determine if the configured Selenium Grid hub is active.
     * 
     * @param hubUrl URL to be checked
     * @return 'true' if configured hub is active; otherwise 'false'
     */
    public static boolean isHubActive(URL hubUrl) {
        return isHostActive(hubUrl, GridServer.HUB_CONFIG);
    }

    /**
     * Determine if the specified Selenium Grid host (hub or node) is active.
     * 
     * @param hostUrl URL to be checked
     * @param request request path (may include parameters)
     * @return 'true' if specified host is active; otherwise 'false'
     */
    public static boolean isHostActive(final URL hostUrl, final String request) {
        try {
            HttpResponse response = getHttpResponse(hostUrl, request);
            return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        } catch (IOException e) { //NOSONAR
            // nothing to do here
        }
        return false;
    }
    
    /**
     * Send the specified GET request to the indicated host.
     * 
     * @param hostUrl target HTTP host connection
     * @param request request path (may include parameters)
     * @return host response for the specified GET request
     * @throws IOException The request triggered an I/O exception
     */
    public static HttpResponse getHttpResponse(final URL hostUrl, final String request) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        URL sessionURL = new URL(hostUrl.getProtocol(), hostUrl.getAuthority(), request);
        BasicHttpEntityEnclosingRequest basicHttpEntityEnclosingRequest = 
                new BasicHttpEntityEnclosingRequest("GET", sessionURL.toExternalForm());
        return client.execute(extractHost(hostUrl), basicHttpEntityEnclosingRequest);
    }
    
    /**
     * Get a driver with "current" capabilities from the active Selenium Grid.
     * <p>
     * <b>NOTE</b>: This method acquires Grid URL and desired driver capabilities from the active configuration.
     * 
     * @return driver object (may be 'null')
     */
    public static WebDriver getDriver() {
        SeleniumConfig config = AbstractSeleniumConfig.getConfig();
        URL remoteAddress = config.getSeleniumGrid().getHubServer().getUrl();
        Capabilities capabilities = config.getCurrentCapabilities();
        return getDriver(remoteAddress, capabilities);
    }
    
    /**
     * Get a driver with desired capabilities from specified Selenium Grid hub.
     * 
     * @param remoteAddress Grid hub from which to obtain the driver
     * @param desiredCapabilities desired capabilities for the driver
     * @return driver object (may be 'null')
     */
    public static WebDriver getDriver(URL remoteAddress, Capabilities desiredCapabilities) {
        Objects.requireNonNull(remoteAddress, "[remoteAddress] must be non-null");
        if (isHubActive(remoteAddress)) {
            return new RemoteWebDriver(remoteAddress, desiredCapabilities);
        } else {
            throw new IllegalStateException("No Selenium Grid instance was found at " + remoteAddress);
        }
    }
    
    /**
     * Read available input from the specified input stream.
     * 
     * @param inputStream input stream
     * @return available input
     * @throws IOException if an I/O error occurs
     */
    public static String readAvailable(InputStream inputStream) throws IOException {
        int length;
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    /**
     * Get the list of node endpoints attached to the specified Selenium Grid hub.
     * 
     * @param hubUrl ULR of Grid hub
     * @return list of node endpoints
     * @throws IOException if an I/O error occurs
     */
    public static List<String> getGridProxies(URL hubUrl) throws IOException {
        String url = hubUrl.getProtocol() + "://" + hubUrl.getAuthority() + GridServer.GRID_CONSOLE;
        Document doc = Jsoup.connect(url).get();
        Elements proxyIds = doc.select("p.proxyid");
        List<String> nodeList = new ArrayList<>();
        for (Element proxyId : proxyIds) {
            String text = proxyId.text();
            int beginIndex = text.indexOf("http");
            int endIndex = text.indexOf(',');
            nodeList.add(text.substring(beginIndex, endIndex));
        }
        return nodeList;
    }
    
    /**
     * Get capabilities of the indicated node of the specified Selenium Grid hub.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubHost Grid hub host
     * @param nodeEndpoint node endpoint
     * @return list of {@link Capabilities} objects for the specified node
     * @throws IOException if an I/O error occurs
     */
    public static Capabilities[] getNodeCapabilities(SeleniumConfig config, URL hubUrl, String nodeEndpoint) throws IOException {
        String json;
        String url = hubUrl.getProtocol() + "://" + hubUrl.getAuthority() + GridServer.NODE_CONFIG + "?id=" + nodeEndpoint;
        try (InputStream is = new URL(url).openStream()) {
            json = readAvailable(is);
        }
        return config.getCapabilitiesForJson(json);
    }

    /**
     * Determine if the specified server is the local host.
     * 
     * @param host URL to be checked
     * @return 'true' if server is local host; otherwise 'false'
     */
    public static boolean isLocalHost(URL host) {
        try {
            InetAddress addr = InetAddress.getByName(host.getHost());
            return (GridUtility.isThisMyIpAddress(addr));
        } catch (UnknownHostException e) {
            LOGGER.warn("Unable to get IP address for '{}'", host.getHost(), e);
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
     * Extract HTTP host object from the specified URL.
     * 
     * @param url {@link URL} from which to extract HTTP host
     * @return {@link HttpHost} object
     */
    public static HttpHost extractHost(URL url) {
        if (url != null) {
            try {
                return URIUtils.extractHost(url.toURI());
            } catch (URISyntaxException e) {
                throw UncheckedThrow.throwUnchecked(e);
            }
        }
        return null;
    }

    /**
     * Get Internet protocol (IP) address for the machine we're running on.
     * 
     * @return IP address for the machine we're running on (a.k.a. - 'localhost')
     */
    public static String getLocalHost() {
        SeleniumConfig config = AbstractSeleniumConfig.getConfig();
        String host = config.getString(SeleniumSettings.GOOGLE_DNS_SOCKET_HOST.key());
        int port = config.getInt(SeleniumSettings.GOOGLE_DNS_SOCKET_PORT.key());
        
        try (final DatagramSocket socket = new DatagramSocket()) {
            // use Google Public DNS to discover preferred local IP
            socket.connect(InetAddress.getByName(host), port);
            return socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException eaten) { //NOSONAR
            SeleniumGrid.LOGGER.warn("Unable to get 'localhost' IP address: {}", eaten.getMessage());
            return "localhost";
        }
    }
}
