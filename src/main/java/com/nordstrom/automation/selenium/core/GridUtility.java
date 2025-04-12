package com.nordstrom.automation.selenium.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.TimeoutException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.net.NetworkUtils;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.exceptions.GridServerLaunchFailedException;
import com.nordstrom.common.base.UncheckedThrow;
import com.nordstrom.common.file.PathUtils;
import com.nordstrom.common.uri.UriUtils;

/**
 * This class provides basic support for interacting with a Selenium Grid instance.
 */
public final class GridUtility {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GridUtility.class);
    private static final NetworkUtils IDENTITY = new NetworkUtils();
    
    /**
     * Private constructor to prevent instantiation.
     */
    private GridUtility() {
        throw new AssertionError("GridUtility is a static utility class that cannot be instantiated");
    }
    
    /**
     * Determine if the specified Selenium Grid host (hub or node) is active.
     * 
     * @param hostUrl {@link URL} to be checked
     * @param pathAndParams path and query parameters
     * @return 'true' if specified host is active; otherwise 'false'
     */
    public static boolean isHostActive(final URL hostUrl, final String... pathAndParams) {
        try {
            HttpResponse response = getHttpResponse(hostUrl, pathAndParams);
            return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        } catch (IOException eaten) {
            // nothing to do here
        }
        return false;
    }
    
    /**
     * Send the specified GET request to the indicated host.
     * 
     * @param hostUrl {@link URL} of target host
     * @param pathAndParams path and query parameters
     * @return host response for the specified GET request
     * @throws IOException if the request triggered an I/O exception
     */
    public static HttpResponse getHttpResponse(final URL hostUrl, final String... pathAndParams) throws IOException {
        Objects.requireNonNull(hostUrl, "[hostUrl] must be non-null");
        HttpClient client = HttpClientBuilder.create().build();
        URI uri = UriUtils.makeBasicURI(hostUrl.getProtocol(), hostUrl.getHost(), hostUrl.getPort(), pathAndParams);
        return client.execute(extractHost(hostUrl), new HttpGet(uri.toURL().toExternalForm()));
    }
    
    /**
     * Send the specified GraphQL query to the indicated host.
     * 
     * @param hostUrl {@link URL} of target host
     * @param query JSON query string
     * @return host response for the specified GraphQL query
     * @throws IOException if the query triggered an I/O exception
     */
    public static HttpResponse callGraphQLService(final URL hostUrl, String query) throws IOException {
        Objects.requireNonNull(hostUrl, "[hostUrl] must be non-null");
        Objects.requireNonNull(query, "[query] must be non-null");
        HttpClient client = HttpClientBuilder.create().build();
        URI uri = UriUtils.makeBasicURI(hostUrl.getProtocol(), hostUrl.getHost(), hostUrl.getPort(), "/graphql");
        HttpPost httpRequest = new HttpPost(uri.toURL().toExternalForm());
        httpRequest.setEntity(new StringEntity(query, ContentType.APPLICATION_JSON));
        return client.execute(extractHost(hostUrl), httpRequest);
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
        if (!GridServer.isHubActive(remoteAddress)) {
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
        Constructor<RemoteWebDriver> ctor = getRemoteWebDriverCtor(config, desiredCapabilities);
        
        Map<String, Object> capsMap = new HashMap<>(desiredCapabilities.asMap());
        
        // if capabilities contain 'nord:options'
        if (capsMap.containsKey("nord:options")) {
            // get map of custom options from capabilities
            Map<String, Object> options = getNordOptions(desiredCapabilities);
            // remove 'personality'
            options.remove("personality");
            // remove 'pluginClass'
            options.remove("pluginClass");
            // if options are empty
            if (options.isEmpty()) {
                // remove 'nord:options'
                capsMap.remove("nord:options");
            // otherwise (non-empty)
            } else {
                // update 'nord:options'
                capsMap.put("nord:options", options);
            }
        }
        
        try {
            // instantiate desired driver via configured grid
            return ctor.newInstance(remoteAddress, new MutableCapabilities(capsMap));
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
    public static String getPersonality(Capabilities capabilities) {
        Map<String, Object> options = getNordOptions(capabilities);
        if (options.containsKey("personality")) return (String) options.get("personality");
        String personality = (String) capabilities.getCapability("appium:automationName");
        if (personality != null) return personality;
        personality = (String) capabilities.getCapability("automationName");
        if (personality != null) return personality;
        return (String) capabilities.getCapability("browserName");
    }
    
    /**
     * Get map of <b>Selenium Foundation</b> custom options.
     * 
     * @param capabilities Selenium {@link Capabilities} object
     * @return mutable map of custom options (may be empty)
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getNordOptions(Capabilities capabilities) {
        Object nordOptions = capabilities.getCapability("nord:options");
        if (nordOptions != null) {
            return new HashMap<>((Map<String, Object>) nordOptions);
        } else {
            return new HashMap<>();
        }
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
        } catch (SocketException e) {
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
        return IDENTITY.getNonLoopbackAddressOfThisMachine();
    }
    
    /**
     * Get next configured output path for Grid server of specified role.
     * 
     * @param config {@link SeleniumConfig} object
     * @param isHub role of Grid server being started: <ul>
     *     <li>{@code true} = hub</li>
     *     <li>{@code false} = node</li>
     *     <li>{@code null} = relay</li>
     * </ul>
     * @return Grid server output path (may be {@code null})
     */
    public static Path getOutputPath(SeleniumConfig config, Boolean isHub) {
        Path outputPath = null;
        
        if (!config.getBoolean(SeleniumSettings.GRID_NO_REDIRECT.key())) {
            String gridRole = (isHub == null) ? "relay" : (isHub) ? "hub" : "node";
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
    private static <T extends RemoteWebDriver> Constructor<T> getRemoteWebDriverCtor(
            SeleniumConfig config, Capabilities desiredCapabilities) {
        
        for (DriverPlugin driverPlugin : getDriverPlugins(config)) {
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

    /**
     * Get instances of all configured driver plugins.
     * 
     * @param config {@link SeleniumConfig} object
     * @return list of driver plug-in instances
     */
    public static List<DriverPlugin> getDriverPlugins(SeleniumConfig config) {
        List<DriverPlugin> driverPlugins = new ArrayList<>();
        
        // get grid plugins setting
        String gridPlugins = config.getString(SeleniumSettings.GRID_PLUGINS.key());
        // if setting is defined and not empty
        if ( ! (gridPlugins == null || gridPlugins.trim().isEmpty())) {
            // iterate specified driver plug-in class names
            for (String driverPlugin : gridPlugins.split(File.pathSeparator)) {
                String className = driverPlugin.trim();
                try {
                    // load driver plug-in class
                    Class<?> pluginClass = Class.forName(className);
                    // get no-argument constructor
                    Constructor<?> ctor = pluginClass.getConstructor();
                    // add instance to plugins list
                    driverPlugins.add((DriverPlugin) ctor.newInstance());
                } catch (ClassNotFoundException e) {
                    throw new ServiceConfigurationError("Specified driver plug-in '" + className + "' not found", e);
                } catch (ClassCastException e) {
                    throw new ServiceConfigurationError("Specified driver plug-in '" + className
                            + "' is not a subclass of DriverPlugin", e);
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new ServiceConfigurationError("Specified driver plug-in '" + className
                            + "' lacks an accessible no-argument constructor", e);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
                    throw new ServiceConfigurationError("Specified driver plug-in '" + className
                            + "' could not be instantiated", e);
                } catch (InvocationTargetException e) {
                    throw new ServiceConfigurationError("Constructor for driver plug-in '" + className
                            + "' threw an exception", e.getTargetException());
                }
            }
        } else {
            // get service loader for driver plugins
            ServiceLoader<DriverPlugin> serviceLoader = ServiceLoader.load(DriverPlugin.class);
            // collect list of configured plugins
            serviceLoader.iterator().forEachRemaining(driverPlugins::add);
        }
        
        return driverPlugins;
    }
    
}
