package com.nordstrom.automation.selenium.core;

import static java.nio.charset.StandardCharsets.UTF_8;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
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
    
    private static final Set<InetAddress> LOCAL_ADDRESSES = Collections.unmodifiableSet(getAllLocalAddresses());

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

        URI uri = UriUtils.makeBasicURI(hostUrl.getProtocol(), hostUrl.getHost(), hostUrl.getPort(), pathAndParams);

        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000)
                .setConnectionRequestTimeout(2000).setSocketTimeout(5000).build();
        CloseableHttpClient client = HttpClientBuilder.create().disableAutomaticRetries()
                .setDefaultRequestConfig(requestConfig).build();

        HttpGet request = new HttpGet(uri.toString());
        HttpResponse response = client.execute(extractHost(hostUrl), request);
        HttpEntity entity = response.getEntity();
        if (entity != null && entity.isStreaming()) {
            response.setEntity(new BufferedHttpEntity(entity));
        }

        return response;
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
                    throw new IllegalStateException("Interrupted activating local grid instance", e);
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
        return result.toString(UTF_8.name());
    }

    /**
     * Get the 'driverPath' value from the specified capabilities map.
     * 
     * @param capabilities map of capabilities
     * @return 'driverPath' value; {@code null} if no 'driverPath' value is found
     */
    public static String getDriverPath(Capabilities capabilities) {
        Map<String, Object> options = getNordOptions(capabilities);
        return (String) Optional.ofNullable(options.get("driverPath")).orElse(null);
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
        Objects.requireNonNull(host, "[host] must be non-null");
        String hostStr = host.getHost();

        if ("localhost".equalsIgnoreCase(hostStr) || "127.0.0.1".equals(hostStr) || "::1".equals(hostStr)) {
            return true;
        }

        for (InetAddress thisAddress : LOCAL_ADDRESSES) {
            if (thisAddress.getHostAddress().equalsIgnoreCase(hostStr)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the set of IP addresses assigned to the local machine.
     * <p>
     * This method enumerates all available network interfaces, filtering for those that are 
     * currently "up" (active). It collects all associated {@link InetAddress} objects, 
     * including IPv4, IPv6, and loopback addresses. 
     * <p>
     * In the event of a {@link SocketException} during enumeration, the method fails 
     * gracefully by returning a set containing only the standard loopback address.
     * 
     * @return a {@link Set} of {@link InetAddress} objects representing local adapters
     */
    private static Set<InetAddress> getAllLocalAddresses() {
        Set<InetAddress> localAddresses = new HashSet<>();
        try {
            Enumeration<NetworkInterface> interfaceEnum = NetworkInterface.getNetworkInterfaces();
            while (interfaceEnum.hasMoreElements()) {
                NetworkInterface thisInterface = interfaceEnum.nextElement();
                if (thisInterface.isUp()) {
                    Enumeration<InetAddress> addressEnum = thisInterface.getInetAddresses();
                    while (addressEnum.hasMoreElements()) {
                        localAddresses.add(addressEnum.nextElement());
                    }
                }
            }
        } catch (SocketException e) {
            localAddresses.add(InetAddress.getLoopbackAddress());
        }
        return localAddresses;
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
