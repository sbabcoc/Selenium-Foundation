package com.nordstrom.automation.selenium;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileLocatorUtils;
import org.apache.commons.configuration2.io.FileSystem;
import org.apache.http.HttpHost;
import org.apache.http.client.utils.URIBuilder;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SearchContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.support.SearchContextWait;
import com.nordstrom.automation.settings.SettingsCore;
import com.nordstrom.common.file.PathUtils;

/**
 * This class declares settings and methods related to WebDriver and Grid configuration for Selenium 2 and Selenium 3.
 */
public abstract class AbstractSeleniumConfig extends
                SettingsCore<AbstractSeleniumConfig.SeleniumSettings> {

    private static final String SETTINGS_FILE = "settings.properties";
    private static final String CAPS_PATTERN = "{\"browserName\": \"%s\"}";
    private static final String DEFAULT_CAPS = String.format(CAPS_PATTERN, "htmlunit");
    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumConfig.class);
    
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
        /**
         * This is URL for the Selenium Grid endpoint: [scheme:][//authority]/wd/hub
         * <p>
         * name: <b>selenium.hub.host</b> <br> default: {@code null} */
        HUB_HOST("selenium.hub.host", null),
        /** name: <b>selenium.hub.port</b> <br> default: {@code null} */
        HUB_PORT("selenuim.hub.port", null),
        /** name: <b>selenium.node.config</b> <br> default: {@code null} */
        NODE_CONFIG("selenium.node.config", null),
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
        HOST_TIMEOUT("selenium.timeout.host", "30"),
        /** name: <b>google.dns.socket.host</b> <br> default: <b>8.8.8.8</b> */
        GOOGLE_DNS_SOCKET_HOST("google.dns.socket.host", "8.8.8.8"),    //NOSONAR
        /** name: <b>google.dns.socket.port</b> <br> default: <b>10002</b> */
        GOOGLE_DNS_SOCKET_PORT("google.dns.socket.port", "10002");
        
        private String propertyName;
        private String defaultValue;
        
        /**
         * Constructor for SeleniumSettings enumeration
         *  
         * @param propertyName setting property name
         * @param defaultValue setting default value
         */
        SeleniumSettings(final String propertyName, final  String defaultValue) {
            this.propertyName = propertyName;
            this.defaultValue = defaultValue;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String key() {
            return propertyName;
        }
        
        /**
         * {@inheritDoc}
         */
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
        
        /**
         * Constructor for WaitType enumeration
         * 
         * @param timeoutSetting timeout setting constant
         */
        WaitType(final SeleniumSettings timeoutSetting) {
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
        public long getInterval(final SeleniumConfig config) {
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
        public SearchContextWait getWait(final SearchContext context) {
            return new SearchContextWait(context, getInterval());
        }
        
    }

    protected static SeleniumConfig seleniumConfig;
    
    private URI targetUri;
    private Path nodeConfigPath;
    private Path hubConfigPath;
    private HttpHost hubHost;
    
    public AbstractSeleniumConfig() throws ConfigurationException, IOException {
        super(SeleniumSettings.class);
    }

    /**
     * Get the Selenium configuration object.
     * 
     * @return Selenium configuration object
     */
    public static SeleniumConfig getConfig() {
        if (seleniumConfig != null) {
            return seleniumConfig;
        }
        throw new IllegalStateException("SELENIUM_CONFIG must be populated by subclass static initializer");
    }
    
    /**
     * Get fully-qualified name of {@code GridLauncher} class.
     * 
     * @return {@code GridLauncher} class name
     */
    public abstract String getLauncherClassName();
    
    /**
     * Get a host object for the Selenium Grid hub server.
     * 
     * @return {@link HttpHost} object for the Selenium Grid hub server 
     */
    public HttpHost getHubHost() {
        if (hubHost == null) {
            String hostStr = getString(SeleniumSettings.HUB_HOST.key());
            if (hostStr != null) {
                hubHost = new HttpHost(hostStr);
            }
        }
        return hubHost;
    }
    
    /**
     * Get shutdown request string for Selenium Grid node server.
     * 
     * @return Selenium Grid node server shutdown request string
     */
    public abstract String getNodeShutdownRequest();
    
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
            URIBuilder builder = new URIBuilder().setPath(getString(SeleniumSettings.TARGET_PATH.key()) + "/")
                    .setScheme(getString(SeleniumSettings.TARGET_SCHEME.key()))
                    .setHost(getString(SeleniumSettings.TARGET_HOST.key()));
            
            String creds = getString(SeleniumSettings.TARGET_CREDS.key());
            if (creds != null) {
                builder.setUserInfo(creds);
            }
            
            String port = getString(SeleniumSettings.TARGET_PORT.key());
            if (port != null) {
                builder.setPort(Integer.parseInt(port));
            }
            
            try {
                targetUri = builder.build().normalize();
            } catch (URISyntaxException eaten) { //NOSONAR
                LOGGER.error("Specified target URI '{}' could not be parsed: {}", builder, eaten.getMessage());
            }
        }
        return targetUri;
    }
    
    /**
     * Get the path to the Selenium Grid node configuration.
     * 
     * @return Selenium Grid node configuration path
     */
    protected Path getNodeConfigPath() {
        if (nodeConfigPath == null) {
            String nodeConfig = getConfigPath(getString(SeleniumSettings.NODE_CONFIG.key()));
            LOGGER.debug("nodeConfig = {}", nodeConfig);
            nodeConfigPath = Paths.get(nodeConfig);
        }
        return nodeConfigPath;
    }
    
    /**
     * Get the path to the Selenium Grid hub configuration.
     * 
     * @return Selenium Grid hub configuration path
     */
    public Path getHubConfigPath() {
        if (hubConfigPath == null) {
            String hubConfig = getConfigPath(getString(SeleniumSettings.HUB_CONFIG.key()));
            LOGGER.debug("hubConfig = {}", hubConfig);
            hubConfigPath = Paths.get(hubConfig);
        }
        return hubConfigPath;
    }
    
    /** 
     * Convert the configured browser specification from JSON to {@link Capabilities} object.   
     *  
     * @return {@link Capabilities} object for the configured browser specification 
     */ 
    public Capabilities getCurrentCapabilities() {
        String browserName = getString(SeleniumSettings.BROWSER_NAME.key());
        if (browserName != null) {
            return getCapabilitiesForName(browserName);
        }
        String capabilities = getString(SeleniumSettings.BROWSER_CAPS.key());
        if (capabilities != null) {
            return getCapabilitiesForJson(capabilities);
        }
        throw new IllegalStateException("Neither browser name nor capabilities are specified");
    }
    
    public Capabilities getCapabilitiesForName(String browserName) {
        return getCapabilitiesForJson(String.format(CAPS_PATTERN, browserName));
    }
    
    public abstract Capabilities getCapabilitiesForJson(String capabilities);
    
    /**
     * Get the path to the specified configuration file.
     * 
     * @param path configuration file path (absolute, relative, or simple filename)
     * @return resolved absolute path of specified file; 'null' if file not found
     */
    private static String getConfigPath(final String path) {
        FileHandler handler = new FileHandler();
        handler.setPath(path);
        
        FileLocator locator = handler.getFileLocator();
        FileSystem fileSystem = FileLocatorUtils.DEFAULT_FILE_SYSTEM;
        FileLocationStrategy strategy = FileLocatorUtils.DEFAULT_LOCATION_STRATEGY;
        
        URL url = strategy.locate(fileSystem, locator);
        if (url != null) {
            try {
                URI uri = getConfigUri(path, url);
                File file = new File(uri);
                return file.getAbsolutePath();
            } catch (URISyntaxException eaten) { //NOSONAR
                LOGGER.warn("Invalid URL returned by file locator: {}", eaten.getMessage());
            } catch (IOException eaten) { //NOSONAR
                LOGGER.warn("Failed to construct file system or extract configuration file: {}", eaten.getMessage());
            }
        }
        return null;
    }
    
    /**
     * Get the URI of the specified configuration file from its resolved URL.
     * 
     * @param path configuration file path (absolute, relative, or simple filename)
     * @param url resolved configuration file URL
     * @return resolved configuration file URI 
     * @throws URISyntaxException if specified URL is invalid
     * @throws IOException on failure to construct file system or extract configuration file
     */
    private static URI getConfigUri(final String path, final URL url) throws URISyntaxException, IOException {
        URI uri = url.toURI();
        if ("jar".equals(uri.getScheme())) {
            try {
                FileSystems.newFileSystem(uri, Collections.emptyMap());
            } catch (FileSystemAlreadyExistsException eaten) { //NOSONAR
                LOGGER.warn("Specified file system already exists: {}", eaten.getMessage());
            } 
            
            String outputDir = PathUtils.getBaseDir();
            File outputFile = new File(outputDir, path);
            Path outputPath = outputFile.toPath();
            if (!outputPath.toFile().exists()) {
                Files.copy(Paths.get(uri), outputPath);
            }
            uri = outputPath.toUri();
        }
        return uri;
    }
    
    /**
     * Get fully-qualified names of context classes for Selenium Grid dependencies.
     * 
     * @return context class names for Selenium Grid dependencies
     */ 
    public abstract String[] getDependencyContexts();
    
    /**
     * 
     * @param jsonStr
     * @param hubEndpoint
     * @return
     * @throws IOException
     */
    public abstract Path createNodeConfig(String jsonStr, HttpHost hubEndpoint) throws IOException;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getSettingsPath() {
        return SETTINGS_FILE;
    }
}