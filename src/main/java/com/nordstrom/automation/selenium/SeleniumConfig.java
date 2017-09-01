package com.nordstrom.automation.selenium;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
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
import java.util.Collections;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileLocatorUtils;
import org.apache.commons.configuration2.io.FileSystem;
import org.apache.commons.io.IOUtils;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SearchContext;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.nordstrom.automation.selenium.support.SearchContextWait;
import com.nordstrom.automation.settings.SettingsCore;

/**
 * This class declares settings and methods related to Selenium WebDriver and Grid configuration.
 * 
 * @see SettingsCore
 */
public class SeleniumConfig extends SettingsCore<SeleniumConfig.SeleniumSettings> {
    
    private static final String SETTINGS_FILE = "settings.properties";
    private static final String CONFIG = "CONFIG";
    private static final String JSON_HEAD = "{ \"capabilities\": [";
    private static final String JSON_TAIL = "], \"configuration\": {} }";
    private static final String CAPS_PATTERN = "{\"browserName\": \"%s\"}";
    
    /** value: {"browserName": "phantomjs"} */
    private static final String DEFAULT_CAPS = String.format(CAPS_PATTERN, "phantomjs");
    
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
        /** name: <b>selenium.node.port</b> <br> default: {@code null} */
        NODE_PORT("selenium.node.port", null),
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
    
    public enum WaitType {
        PAGE_LOAD(SeleniumSettings.PAGE_LOAD_TIMEOUT),
        IMPLIED(SeleniumSettings.IMPLIED_TIMEOUT),
        SCRIPT(SeleniumSettings.SCRIPT_TIMEOUT),
        WAIT(SeleniumSettings.WAIT_TIMEOUT),
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
            return getInterval(null);
        }
        
        /**
         * Get the timeout interval for this wait type.<br>
         * <b>NOTE</b>: If {@code config} is 'null', this object will be acquired from the current configuration context. 
         * 
         * @param config {@link SeleniumConfig} object to interrogate; may be 'null' (see <b>NOTE</b>)
         * @return wait type timeout interval
         */
        public long getInterval(SeleniumConfig config) {
            if (timeoutInterval == null) {
                if (config == null) config = getConfig();
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

    private static final ThreadLocal<SeleniumConfig> seleniumConfig = new ThreadLocal<>();
    
    private URI targetUri;
    private String nodeConfigPath;
    private RegistrationRequest nodeConfig;
    private String[] nodeArgs;
    private String hubConfigPath;
    private GridHubConfiguration hubConfig;
    private String[] hubArgs;
    private Capabilities browserCaps;
    
    public SeleniumConfig() throws ConfigurationException, IOException {
        super(SeleniumSettings.class);
    }

    /**
     * Get the Selenium configuration object for the current context.
     * 
     * @return Selenium configuration object
     */
    public static SeleniumConfig getConfig() {
        return getConfig(Reporter.getCurrentTestResult());
    }
    
    /**
     * Get the Selenium configuration object for the specified context.
     * 
     * @param testResult configuration context (TestNG test result object)
     * @return Selenium configuration object
     */
    public static SeleniumConfig getConfig(ITestResult testResult) {
        if (testResult == null) {
            return getSeleniumConfig();
        }
        if (testResult.getAttribute(CONFIG) == null) {
            synchronized (testResult) {
                if (testResult.getAttribute(CONFIG) == null) {
                    testResult.setAttribute(CONFIG, getSeleniumConfig());
                }
            }
        }
        return (SeleniumConfig) testResult.getAttribute(CONFIG);
    }
    
    private static SeleniumConfig getSeleniumConfig() {
        if (seleniumConfig.get() == null) {
            try {
                seleniumConfig.set(new SeleniumConfig());
            } catch (ConfigurationException | IOException e) {
                throw new RuntimeException("Failed to instantiate settings", e);
            }
        }
        return seleniumConfig.get();
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
            if (creds != null) builder.userInfo(creds);
            
            String port = getString(SeleniumSettings.TARGET_PORT.key());
            if (port != null) builder.port(Integer.parseInt(port));
            
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
            RegistrationRequest nodeConfig = getNodeConfig();
            Map<String, Object> config = nodeConfig.getConfiguration();
            nodeArgs = new String[] {"-role", "node", "-nodeConfig", configPath, "-host", (String) config.get("host"),
                    "-port", config.get("port").toString(), "-hub", (String) config.get("hub")};
        }
        return nodeArgs;
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
        if (nodeHost != null) config.put("host", nodeHost);
        if (config.get("host") == null) config.put("host", getLocalHost());
        
        Integer nodePort = getInteger(SeleniumSettings.NODE_PORT.key(), null);
        if (nodePort != null) config.put("port", nodePort);
        if (config.get("port") == null) config.put("port", Integer.valueOf(5555));
        
        GridHubConfiguration hubConfig = getHubConfig();
        config.put("hub", "http://" + hubConfig.getHost() + ":" + hubConfig.getPort() + "/grid/register/");
        
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
        return hubArgs;
    }
    
    /**
     * Resolve Selenium Grid hub settings for host and port.
     * 
     * @param hubConfig node configuration with unresolved settings
     * @return hub configuration with resolved settings
     */
    private GridHubConfiguration resolveHubSettings(GridHubConfiguration hubConfig) {
        String hubHost = getString(SeleniumSettings.HUB_HOST.key());
        if (hubHost != null)  hubConfig.setHost(hubHost);
        if (hubConfig.getHost() == null) hubConfig.setHost(getLocalHost());
        
        Integer hubPort = getInteger(SeleniumSettings.HUB_PORT.key(), null);
        if (hubPort != null) hubConfig.setPort(hubPort.intValue());
        
        return hubConfig;
    }

    /**
     * Get Internet protocol IP address for the machine we're running on.
     * 
     * @return IP address for the machine we're running on (a.k.a. - 'localhost')
     */
    private static String getLocalHost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
        }
        return "localhost";
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
                    } catch (IOException e) { }
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
    private String getConfigPath(String path) {
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
                    } catch (FileSystemAlreadyExistsException e) { } 
                    
                    String outputDir = getOutputDir();
                    File outputFile = new File(outputDir, path);
                    Path outputPath = outputFile.toPath();
                    if (Files.notExists(outputPath)) {
                        Files.copy(Paths.get(uri), outputPath);
                    }
                    uri = outputPath.toUri();
                }
                File file = new File(uri);
                return file.getAbsolutePath();
            } catch (URISyntaxException | IOException e) { }
        }
        return null;
    }
    
    /**
     * Get test run output directory.
     * 
     * @return test run output directory
     */
    public static String getOutputDir() {
        return getOutputDir(Reporter.getCurrentTestResult());
    }
    
    /**
     * Get test run output directory.
     * 
     * @param testResult configuration context (TestNG test result object)
     * @return test run output directory
     */
    public static String getOutputDir(ITestResult testResult) {
        String outputDir;
        if (testResult != null) {
            outputDir = testResult.getTestContext().getOutputDirectory();
        } else {
            Path currentRelativePath = Paths.get("");
            outputDir = currentRelativePath.toAbsolutePath().toString();
        }
        return outputDir;
    }
    
    @Override
    public String getSettingsPath() {
        return SETTINGS_FILE;
    }
}
