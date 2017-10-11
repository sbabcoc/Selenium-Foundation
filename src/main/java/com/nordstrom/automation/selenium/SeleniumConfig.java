package com.nordstrom.automation.selenium;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileLocatorUtils;
import org.apache.commons.configuration2.io.FileSystem;
import org.apache.commons.io.IOUtils;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.openqa.grid.common.GridRole;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SearchContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.core.TestBase;
import com.nordstrom.automation.selenium.support.SearchContextWait;
import com.nordstrom.automation.settings.SettingsCore;

/**
 * This class declares settings and methods related to Selenium WebDriver and Grid configuration.
 * 
 * @see SettingsCore
 */
public class SeleniumConfig extends SettingsCore<SeleniumConfig.SeleniumSettings> {
    
    private static final String SETTINGS_FILE = "settings.properties";
    private static final String JSON_HEAD = "{ \"capabilities\": [";
    private static final String JSON_TAIL = "], \"configuration\": {} }";
    private static final String CAPS_PATTERN = "{\"browserName\": \"%s\"}";
    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumConfig.class);
    
    /** value: {"browserName": "phantomjs"} */
    private static final String DEFAULT_CAPS = String.format(CAPS_PATTERN, "phantomjs");
    /** value: 5555 */
    private static final Integer DEFAULT_NODE_PORT = Integer.valueOf(5555);
    
    /**
     * This enumeration declares the settings that enable you to control the parameters used by
     * <b>Selenium Foundation</b>.
     * <p>
     * Each setting is defined by a constant name and System property key. Many settings also define
     * default values. Note that all of these settings can be overridden via the
     * {@code settings.properties} file and System property declarations.
     */
    public enum SeleniumSettings implements SettingsCore.SettingsAPI {
        /** name: <b>selenium.target.scheme</b> <br> default: <b>http</b> */
        TARGET_SCHEME("selenium.target.scheme", "http"),
        /** name: <b>selenium.target.creds</b> <br> default: {@code null} */
        TARGET_CREDS("selenium.target.creds", null),
        /** name: <b>selenium.target.host</b> <br> default: <b>localhost</b> */
        TARGET_HOST("selenium.target.host", "localhost"),
        /** name: <b>selenium.target.port</b> <br> default: {@code null} */
        TARGET_PORT("selenium.target.port", null),
        /** name: <b>selenium.target.path</b> <br> default: <b>/</b> */
        TARGET_PATH("selenium.target.path", "/"),
        /** name: <b>selenium.hub.config</b> <br> default: <b>hubConfig.json</b> */
        HUB_CONFIG("selenium.hub.config", "hubConfig.json"),
        /** name: <b>selenium.hub.host</b> <br> default: {@code null} */
        HUB_HOST("selenium.hub.host", null),
        /** name: <b>selenium.hub.port</b> <br> default: {@code null} */
        HUB_PORT("selenuim.hub.port", null),
        /** name: <b>selenium.node.config</b> <br> default: <b>nodeConfig.json</b> */
        NODE_CONFIG("selenium.node.config", "nodeConfig.json"),
        /** name: <b>selenium.node.host</b> <br> default: {@code null} */
        NODE_HOST("selenium.node.host", null),
        /** name: <b>selenium.node.port</b> <br> default: <b>5555</b> */
        NODE_PORT("selenium.node.port", "5555"),
        /** name: <b>selenium.browser.name</b> <br> default: {@code null} */
        BROWSER_NAME("selenium.browser.name", null),
        /** name: <b>selenium.browser.caps</b> <br> default: {@link SeleniumConfig#DEFAULT_CAPS DEFAULT_CAPS} */
        BROWSER_CAPS("selenium.browser.caps", DEFAULT_CAPS),
        /** name: <b>selenium.timeout.pageload</b> <br> default: <b>30</b> */
        PAGE_LOAD_TIMEOUT("selenium.timeout.pageload", "30"),
        /** name: <b>selenium.timeout.implied</b> <br> default: <b>15</b> */
        IMPLIED_TIMEOUT("selenium.timeout.implied", "15"),
        /** name: <b>selenium.timeout.script</b> <br> default: <b>30</b> */
        SCRIPT_TIMEOUT("selenium.timeout.script", "30"),
        /** name: <b>selenium.timeout.wait</b> <br> default: <b>15</b> */
        WAIT_TIMEOUT("selenium.timeout.wait", "15"),
        /** name: <b>selenium.timeout.host</b> <br> default: <b>30</b> */
        HOST_TIMEOUT("selenium.timeout.host", "30");
        
        private String propertyName;
        private String defaultValue;
        
        SeleniumSettings(String propertyName, String defaultValue) {
            this.propertyName = propertyName;
            this.defaultValue = defaultValue;
        }
        
        @Override
        public String key() {
            return propertyName;
        }

        @Override
        public String val() {
            return defaultValue;
        }
    }
    
    /**
     * This enumeration provides easy access to the timeout intervals defined in {@link SeleniumSettings}.
     */
    public enum WaitType {
        /**
         * purpose: The maximum allowed interval for a page to finish loading. <br>
         * setting: {@link SeleniumSettings#PAGE_LOAD_TIMEOUT page load timeout}
         */
        PAGE_LOAD(SeleniumSettings.PAGE_LOAD_TIMEOUT),
        
        /**
         * purpose: The maximum amount of time the driver will search for an element. <br>
         * setting: {@link SeleniumSettings#IMPLIED_TIMEOUT implicit timeout}
         */
        IMPLIED(SeleniumSettings.IMPLIED_TIMEOUT),
        
        /**
         * purpose: The maximum allowed interval for an asynchronous script to finish. <br>
         * setting: {@link SeleniumSettings#SCRIPT_TIMEOUT script timeout}
         */
        SCRIPT(SeleniumSettings.SCRIPT_TIMEOUT),
        
        /**
         * purpose: The maximum amount of time to wait for a search context event. <br> 
         * setting: {@link SeleniumSettings#WAIT_TIMEOUT wait timeout}
         */
        WAIT(SeleniumSettings.WAIT_TIMEOUT),
        
        /**
         * purpose: The maximum amount of time to wait for a Grid server to launch. <br>
         * setting: {@link SeleniumSettings#HOST_TIMEOUT host timeout}
         */
        HOST(SeleniumSettings.HOST_TIMEOUT);
        
        private SeleniumSettings timeoutSetting;
        private Long timeoutInterval;
        
        WaitType(SeleniumSettings timeoutSetting) {
            this.timeoutSetting = timeoutSetting;
        }
        
        /**
         * Get the timeout interval for this wait type
         * 
         * @return wait type timeout interval
         */
        public long getInterval() {
            return getInterval(getConfig());
        }
        
        /**
         * Get the timeout interval for this wait type.<br>
         * 
         * @param config {@link SeleniumConfig} object to interrogate
         * @return wait type timeout interval
         */
        public long getInterval(SeleniumConfig config) {
            if (timeoutInterval == null) {
                Objects.requireNonNull(config, "[config] must be non-null");
                timeoutInterval = config.getLong(timeoutSetting.key());
            }
            return timeoutInterval;
        }
        
        /**
         * Get a search context wait object for the specified context
         * 
         * @param context context for which timeout is needed
         * @return {@link SearchContextWait} object for the specified context
         */
        public SearchContextWait getWait(SearchContext context) {
            return new SearchContextWait(context, getInterval());
        }
        
    }

    private static final SeleniumConfig seleniumConfig;
    
    static {
        try {
            seleniumConfig = new SeleniumConfig();
        } catch (ConfigurationException | IOException e) {
            throw new RuntimeException("Failed to instantiate settings", e);
        }
    }
    
    private URI targetUri;
    private String nodeConfigPath;
    private RegistrationRequest nodeConfig;
    private String[] nodeArgs;
    private String hubConfigPath;
    private GridHubConfiguration hubConfig;
    private String[] hubArgs;
    private Capabilities browserCaps;
    
    /**
     * Instantiate a <b>Selenium Foundation</b> configuration object.
     * 
     * @throws ConfigurationException If a failure is encountered while initializing this configuration object.
     * @throws IOException If a failure is encountered while reading from a configuration input stream.
     */
    public SeleniumConfig() throws ConfigurationException, IOException {
        super(SeleniumSettings.class);
    }

    /**
     * Get the Selenium configuration object.
     * 
     * @return Selenium configuration object
     */
    public static SeleniumConfig getConfig() {
        return seleniumConfig;
    }
    
    /**
     * Get the configured target URI as specified by its component parts.
     * <p>
     * <b>NOTE</b>: The target URI is assembled from following components: 
     *     {@link SeleniumSettings#TARGET_SCHEME scheme}, {@link SeleniumSettings#TARGET_CREDS credentials},
     *     {@link SeleniumSettings#TARGET_HOST host}, {@link SeleniumSettings#TARGET_PORT port}, and
     *     {@link SeleniumSettings#TARGET_PATH base path}
     * 
     * @return assembled target URI
     */
    public URI getTargetUri() {
        if (targetUri == null) {
            UriBuilder builder = UriBuilder.fromPath(getString(SeleniumSettings.TARGET_PATH.key()))
                    .scheme(getString(SeleniumSettings.TARGET_SCHEME.key()))
                    .host(getString(SeleniumSettings.TARGET_HOST.key()));
            
            String creds = getString(SeleniumSettings.TARGET_CREDS.key());
            if (creds != null) {
                builder.userInfo(creds);
            }
            
            String port = getString(SeleniumSettings.TARGET_PORT.key());
            if (port != null) {
                builder.port(Integer.parseInt(port));
            }
            
            targetUri = builder.build();
        }
        return targetUri;
    }
    
    /**
     * Get the path to the Selenium Grid node configuration.
     * 
     * @return Selenium Grid node configuration path
     */
    private String getNodeConfigPath() {
        if (nodeConfigPath == null) {
            nodeConfigPath = getConfigPath(getString(SeleniumSettings.NODE_CONFIG.key()));
            LOGGER.debug("nodeConfigPath = {}", nodeConfigPath);
        }
        return nodeConfigPath;
    }
    
    /**
     * Get the Selenium Grid node configuration.
     * 
     * @return Selenium Grid node configuration
     */
    public RegistrationRequest getNodeConfig() {
        if (nodeConfig == null) {
            nodeConfig = new RegistrationRequest();
            nodeConfig.loadFromJSON(getNodeConfigPath());
            nodeConfig = resolveNodeSettings(nodeConfig);
            
            // hack for RegistrationRequest bug
            nodeConfig.setRole(GridRole.NODE);
        }
        return nodeConfig;
    }
    
    /**
     * Get the arguments needed to launch a local Selenium Grid node.
     * 
     * @return array of node launch arguments
     */
    public String[] getNodeArgs() {
        if (nodeArgs == null) {
            String configPath = getNodeConfigPath();
            Map<String, Object> config = getNodeConfig().getConfiguration();
            nodeArgs = new String[] {"-role", "node", "-nodeConfig", configPath, "-host", (String) config.get("host"),
                    "-port", config.get("port").toString(), "-hub", (String) config.get("hub")};
        }
        return Arrays.copyOf(nodeArgs, nodeArgs.length);
    }

    /**
     * Resolve Selenium Grid node settings for host, port, and hub.
     * 
     * @param nodeConfig node configuration with unresolved settings
     * @return node configuration with resolved settings
     */
    private RegistrationRequest resolveNodeSettings(RegistrationRequest nodeConfig) {
        Map<String, Object> config = nodeConfig.getConfiguration();
        
        String nodeHost = getString(SeleniumSettings.NODE_HOST.key());
        if (nodeHost != null) {
            config.put("host", nodeHost);
        }
        if (config.get("host") == null) {
            config.put("host", getLocalHost());
        }
        
        config.put("port", getInteger(SeleniumSettings.NODE_PORT.key(), DEFAULT_NODE_PORT));
        config.put("hub", "http://" + getHubConfig().getHost() + ":" + getHubConfig().getPort() + "/grid/register/");
        
        return nodeConfig;
    }
    
    /**
     * Get the path to the Selenium Grid hub configuration.
     * 
     * @return Selenium Grid hub configuration path
     */
    private String getHubConfigPath() {
        if (hubConfigPath == null) {
            hubConfigPath = getConfigPath(getString(SeleniumSettings.HUB_CONFIG.key()));
            LOGGER.debug("hubConfigPath = {}", hubConfigPath);
        }
        return hubConfigPath;
    }
    
    /**
     * Get the Selenium Grid hub configuration.
     * 
     * @return Selenium Grid hub configuration
     */
    public GridHubConfiguration getHubConfig() {
        if (hubConfig == null) {
            hubConfig = new GridHubConfiguration();
            hubConfig.loadFromJSON(getHubConfigPath());
            hubConfig = resolveHubSettings(hubConfig);
        }
        return hubConfig;
    }
    
    /**
     * Get the arguments needed to launch a local Selenium Grid hub.
     * 
     * @return array of hub launch arguments
     */
    public String[] getHubArgs() {
        if (hubArgs == null) {
            String configPath = getHubConfigPath();
            GridHubConfiguration config = getHubConfig();
            hubArgs = new String[] {"-role", "hub", "-hubConfig", configPath, 
                    "-host", config.getHost(), "-port", Integer.toString(config.getPort())};
        }
        return Arrays.copyOf(hubArgs, hubArgs.length);
    }
    
    /**
     * Resolve Selenium Grid hub settings for host and port.
     * 
     * @param hubConfig node configuration with unresolved settings
     * @return hub configuration with resolved settings
     */
    private GridHubConfiguration resolveHubSettings(GridHubConfiguration hubConfig) {
        String hubHost = getString(SeleniumSettings.HUB_HOST.key());
        if (hubHost != null) {
            hubConfig.setHost(hubHost);
        }
        if (hubConfig.getHost() == null) {
            hubConfig.setHost(getLocalHost());
        }
        
        Integer hubPort = getInteger(SeleniumSettings.HUB_PORT.key(), null);
        if (hubPort != null) {
            hubConfig.setPort(hubPort.intValue());
        }
        
        return hubConfig;
    }

    /**
     * Get Internet protocol (IP) address for the machine we're running on.
     * 
     * @return IP address for the machine we're running on (a.k.a. - 'localhost')
     */
    private static String getLocalHost() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            // use Google Public DNS to discover preferred local IP
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException eaten) {
            LOGGER.warn("Unable to get 'localhost' IP address: {}", eaten.getMessage());
            return "localhost";
        }
    }

    /**
     * Convert the configured browser specification from JSON to {@link Capabilities} object.
     * 
     * @return {@link Capabilities} object for the configured browser specification
     */
    public Capabilities getBrowserCaps() {
        if (browserCaps == null) {
            String jsonStr = null;
            String nameStr = getString(SeleniumSettings.BROWSER_NAME.key());
            if (nameStr != null) {
                InputStream inputStream = 
                        Thread.currentThread().getContextClassLoader().getResourceAsStream(nameStr + "Caps.json");
                if (inputStream != null) {
                    try {
                        jsonStr = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    } catch (IOException eaten) {
                        LOGGER.warn("Unable to get browser configuration file contents: {}", eaten.getMessage());
                    }
                }
                
                if (jsonStr == null) {
                    jsonStr = String.format(CAPS_PATTERN, nameStr);
                }
            }
            
            if (jsonStr == null) {
                jsonStr = getString(SeleniumSettings.BROWSER_CAPS.key());
            }
            
            RegistrationRequest config = RegistrationRequest.getNewInstance(JSON_HEAD + jsonStr + JSON_TAIL);
            browserCaps = config.getCapabilities().get(0);
        }
        return browserCaps;
    }

    /**
     * Get the path to the specified configuration file.
     * 
     * @param path configuration file path (absolute, relative, or simple filename)
     * @return resolved absolute path of specified file; 'null' if file not found
     */
    private static String getConfigPath(String path) {
        FileHandler handler = new FileHandler();
        handler.setPath(path);
        
        FileLocator locator = handler.getFileLocator();
        FileSystem fileSystem = FileLocatorUtils.DEFAULT_FILE_SYSTEM;
        FileLocationStrategy strategy = FileLocatorUtils.DEFAULT_LOCATION_STRATEGY;
        
        URL url = strategy.locate(fileSystem, locator);
        if (url != null) {
            try {
                URI uri = url.toURI();
                if ("jar".equals(uri.getScheme())) {
                    try {
                        FileSystems.newFileSystem(uri, Collections.emptyMap());
                    } catch (FileSystemAlreadyExistsException eaten) {
                        LOGGER.warn("Specified file system already exists: {}", eaten.getMessage());
                    } 
                    
                    String outputDir = TestBase.getOutputDir();
                    File outputFile = new File(outputDir, path);
                    Path outputPath = outputFile.toPath();
                    if (!outputPath.toFile().exists()) {
                        Files.copy(Paths.get(uri), outputPath);
                    }
                    uri = outputPath.toUri();
                }
                File file = new File(uri);
                return file.getAbsolutePath();
            } catch (URISyntaxException eaten) {
                LOGGER.warn("Invalid URL returned by file locator: {}", eaten.getMessage());
            } catch (IOException eaten) {
                LOGGER.warn("Failed to construct file system or extract configuration file: {}", eaten.getMessage());
            }
        }
        return null;
    }
    
    @Override
    public String getSettingsPath() {
        return SETTINGS_FILE;
    }
}
