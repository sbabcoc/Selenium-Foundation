package com.nordstrom.automation.selenium.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.TimeoutException;

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
import org.openqa.grid.common.GridRole;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.SeleniumGrid.GridServer;
import com.nordstrom.automation.selenium.exceptions.GridServerLaunchFailedException;
import com.nordstrom.automation.selenium.utility.NetIdentity;
import com.nordstrom.common.base.UncheckedThrow;
import com.nordstrom.common.file.PathUtils;

/**
 * This class provides basic support for interacting with a Selenium Grid instance.
 */
public final class GridUtility {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GridUtility.class);
    private static final NetIdentity IDENTITY = new NetIdentity();
    
    /**
     * Private constructor to prevent instantiation.
     */
    private GridUtility() {
        throw new AssertionError("GridUtility is a static utility class that cannot be instantiated");
    }
    
    /**
     * Determine if the configured Selenium Grid hub is active.
     * 
     * @param hubUrl {@link URL} to be checked
     * @return 'true' if configured hub is active; otherwise 'false'
     */
    public static boolean isHubActive(URL hubUrl) {
        return isHostActive(hubUrl, GridServer.HUB_CONFIG);
    }

    /**
     * Determine if the specified Selenium Grid host (hub or node) is active.
     * 
     * @param hostUrl {@link URL} to be checked
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
     * @param hostUrl {@link URL} of target host
     * @param request request path (may include parameters)
     * @return host response for the specified GET request
     * @throws IOException The request triggered an I/O exception
     */
    public static HttpResponse getHttpResponse(final URL hostUrl, final String request) throws IOException {
        Objects.requireNonNull(hostUrl, "[hostUrl] must be non-null");
        Objects.requireNonNull(request, "[request] must be non-null");
        HttpClient client = HttpClientBuilder.create().build();
        URL sessionURL = new URL(hostUrl.getProtocol(), hostUrl.getHost(), hostUrl.getPort(), request);
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
        SeleniumConfig config = SeleniumConfig.getConfig();
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
        
        SeleniumConfig config = SeleniumConfig.getConfig();
        
        // if specified hub is inactive
        if (!isHubActive(remoteAddress)) {
            // if hub URL is on local host
            if (isLocalHost(remoteAddress)) {
                LocalSeleniumGrid localGrid = (LocalSeleniumGrid) config.getSeleniumGrid();
                try {
                    // activate grid
                    localGrid.activate();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException | TimeoutException e) {
                    throw new IllegalStateException("Failed activating local grid instance", e);
                }
            } else {
                throw new IllegalStateException("No Selenium Grid instance was found at " + remoteAddress);
            }
        }
        
        // get constructor for RemoteWebDriver class corresponding to desired capabilities
        Constructor<RemoteWebDriver> ctor = getRemoteWebDriverCtor(desiredCapabilities);
        
        // create mutable copy of desired capabilities
        MutableCapabilities filteredCapabilities = new MutableCapabilities(desiredCapabilities);
        // remove 'personality' and plug-in class from capabilities
        filteredCapabilities.setCapability("personality", (Object) null);
        filteredCapabilities.setCapability("pluginClass", (Object) null);
        
        try {
            // instantiate desired driver via configured grid
            return ctor.newInstance(remoteAddress, filteredCapabilities);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw UncheckedThrow.throwUnchecked(e);
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
        while (inputStream.available() > 0) {
            length = inputStream.read(buffer);
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    /**
     * Get the list of node endpoints attached to the specified Selenium Grid hub.
     * 
     * @param hubUrl {@link URL} of Grid hub
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
     * @param hubUrl {@link URL} of Grid hub
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
     * Get the 'personality' value from the specified capabilities map.
     * <p>
     * <b>NOTE</b>: The 'personality' value is derived from the following capabilities (in precedence order):
     * <ul>
     *     <li>personality</li>
     *     <li>appium:automationName</li>
     *     <li>automationName</li>
     *     <li>browserName</li>
     * </ul>
     * 
     * @param capabilities map of capabilities
     * @return 'personality' value; {@code null} if no 'personality' value is found
     */
    public static String getPersonality(Map<String, Object> capabilities) {
        if (capabilities.containsKey("personality")) return (String) capabilities.get("personality");
        if (capabilities.containsKey("appium:automationName")) return (String) capabilities.get("appium:automationName");
        if (capabilities.containsKey("automationName")) return (String) capabilities.get("automationName");
        return (String) capabilities.get("browserName");
    }

    /**
     * Determine if the specified server is the local host.
     * 
     * @param host {@link URL} to be checked
     * @return 'true' if server is local host; otherwise 'false'
     */
    public static boolean isLocalHost(URL host) {
        try {
            InetAddress addr = InetAddress.getByName(host.getHost());
            return (isThisMyIpAddress(addr));
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
        return IDENTITY.getHostAddress();
    }
    
    /**
     * Get next configured output path for Grid server of specified role.
     * 
     * @param config {@link SeleniumConfig} object
     * @param role role of Grid server being started
     * @return Grid server output path (may be {@code null})
     */
    public static Path getOutputPath(SeleniumConfig config, GridRole role) {
        Path outputPath = null;
        
        if (!config.getBoolean(SeleniumSettings.GRID_NO_REDIRECT.key())) {
            String gridRole = role.toString().toLowerCase();
            String logsFolder = config.getString(SeleniumSettings.GRID_LOGS_FOLDER.key());
            Path logsPath = Paths.get(logsFolder);
            if (!logsPath.isAbsolute()) {
                String workingDir = config.getString(SeleniumSettings.GRID_WORKING_DIR.key());
                if (workingDir == null || workingDir.isEmpty()) {
                    workingDir = System.getProperty("user.dir");
                }
                logsPath = Paths.get(workingDir, logsFolder);
            }
            
            try {
                if (!logsPath.toFile().exists()) {
                    Files.createDirectories(logsPath);
                }
                outputPath = PathUtils.getNextPath(logsPath, "grid-" + gridRole, "log");
            } catch (IOException e) {
                throw new GridServerLaunchFailedException(gridRole, e);
            }
        }
        
        return outputPath;
    }
    
    /**
     * Get constructor for the desired driver's {@link RemoteWebDriver} implementation.
     * 
     * @param <T> constructor type parameter
     * @param desiredCapabilities desired capabilities for the driver
     * @return constructor for desired {@link RemoteWebDriver} implementation
     */
    @SuppressWarnings("unchecked")
    private static <T extends RemoteWebDriver> Constructor<T> getRemoteWebDriverCtor(Capabilities desiredCapabilities) {
        for (DriverPlugin driverPlugin : ServiceLoader.load(DriverPlugin.class)) {
            Constructor<T> ctor = driverPlugin.getRemoteWebDriverCtor(desiredCapabilities);
            if (ctor != null) {
                return ctor;
            }
        }
        try {
            return (Constructor<T>) RemoteWebDriver.class.getConstructor(URL.class, Capabilities.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw UncheckedThrow.throwUnchecked(e);
        }
    }
    
}
