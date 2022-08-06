package com.nordstrom.automation.selenium;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileLocatorUtils;
import org.apache.commons.configuration2.io.FileSystem;
import org.apache.http.client.utils.URIBuilder;
import org.openqa.grid.web.servlet.LifecycleServlet;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SearchContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.core.SeleniumGrid;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameA_Servlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameB_Servlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameC_Servlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameD_Servlet;
import com.nordstrom.automation.selenium.support.SearchContextWait;
import com.nordstrom.automation.settings.SettingsCore;
import com.nordstrom.common.base.UncheckedThrow;
import com.nordstrom.common.file.PathUtils;

/**
 * This class declares settings and methods related to WebDriver and Grid configuration for Selenium 2 and Selenium 3.
 */
public abstract class AbstractSeleniumConfig extends
                SettingsCore<AbstractSeleniumConfig.SeleniumSettings> {

    private static final String SETTINGS_FILE = "settings.properties";
    private static final String CAPS_PATTERN = "{\"browserName\":\"%s\"}";

    /** value: <b>{"browserName":"htmlunit"}</b> */
    private static final String DEFAULT_CAPS = String.format(CAPS_PATTERN, "htmlunit");
    
    protected static final String NODE_MODS_SUFFIX = ".node.mods";
    private static final String CAPS_MODS_SUFFIX = ".caps.mods";
    
    private static final String APPIUM_PATH = "APPIUM_BINARY_PATH";
    private static final String NODE_PATH = "NODE_BINARY_PATH";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumConfig.class);
    
    private static final String APPIUM_HOST = "\"appium:host\":\"0.0.0.0\",";
    private static final String PERSONALITY = "\"nord:options\":{\"personality\":\"%s\"}";
        
    /**
     * This enumeration declares the settings that enable you to control the parameters used by
     * <b>Selenium Foundation</b>.
     * <p>
     * Each setting is defined by a constant name and System property key. Many settings also define
     * default values. Note that all of these settings can be overridden via the
     * {@code settings.properties} file and System property declarations.
     */
    public enum SeleniumSettings implements SettingsCore.SettingsAPI {
        /**
         * Scheme component for the {@link AbstractSeleniumConfig#getTargetUri target URI}.
         * <p>
         * name: <b>selenium.target.scheme</b><br>
         * default: <b>http</b>
         */
        TARGET_SCHEME("selenium.target.scheme", "http"),
        
        /**
         * Credentials component for the {@link AbstractSeleniumConfig#getTargetUri target URI}.
         * <p>
         * name: <b>selenium.target.creds</b><br>
         * default: {@code null}
         */
        TARGET_CREDS("selenium.target.creds", null),
        
        /**
         * Host component for the {@link AbstractSeleniumConfig#getTargetUri target URI}.
         * <p>
         * name: <b>selenium.target.host</b><br>
         * default: <b>localhost</b>
         */
        TARGET_HOST("selenium.target.host", "localhost"),
        
        /**
         * Port component for the {@link AbstractSeleniumConfig#getTargetUri target URI}.
         * <p>
         * name: <b>selenium.target.port</b><br>
         * default: {@code null}
         */
        TARGET_PORT("selenium.target.port", null),
        
        /**
         * Path component for the {@link AbstractSeleniumConfig#getTargetUri target URI}.
         * <p>
         * name: <b>selenium.target.path</b><br>
         * default: <b>/</b>
         */
        TARGET_PATH("selenium.target.path", "/"),
        
        /**
         * This setting specifies a path-delimited list of fully-qualified names of local <b>Selenium Grid</b> driver
         * plugin classes.
         * <p>
         * <b>NOTE</b>: Defining a value for this setting overrides the <b>ServiceLoader</b> specification defined
         * by the associated provider configuration file (<i>com.nordstrom.automation.selenium.DriverPlugin</i>).
         * <p>
         * name: <b>selenium.grid.plugins</b><br>
         * default: {@code null}
         */
        GRID_PLUGINS("selenium.grid.plugins", null),
        
        /**
         * This setting specifies whether the local <b>Selenium Grid</b> instance will be shut down at the end of the
         * test run.
         * <p>
         * name: <b>selenium.grid.shutdown</b><br>
         * default: <b>true</b>
         */
        SHUTDOWN_GRID("selenium.grid.shutdown", "true"),
        
        /**
         * This setting specifies the fully-qualified name of the <b>GridLauncher</b> class, which provides a
         * command-line interface for configuring and launching <b>Selenium Grid</b> servers implemented in Java.
         * <p>
         * name: <b>selenium.grid.launcher</b><br>
         * default: (populated by {@link SeleniumConfig#getDefaults() getDefaults()})
         * <ul>
         *     <li>Selenium 3: <b>org.openqa.grid.selenium.GridLauncherV3</b></li>
         * </ul>
         */
        GRID_LAUNCHER("selenium.grid.launcher", null),
        
        /**
         * This setting specifies a path-delimited list of fully-qualified names of context classes for the
         * dependencies of the {@link #GRID_LAUNCHER} class.
         * <p>
         * name: <b>selenium.launcher.deps</b><br>
         * default: (populated by {@link SeleniumConfig#getDefaults() getDefaults()})
         */
        LAUNCHER_DEPS("selenium.launcher.deps", null),
        
        /**
         * This setting specifies the configuration file name/path for the local <b>Selenium Grid</b> hub server.
         * <p>
         * name: <b>selenium.hub.config</b><br>
         * Selenium 3: <b>hubConfig-s3.json</b>
         */
        HUB_CONFIG("selenium.hub.config", null),
        
        /**
         * This is the URL for the <b>Selenium Grid</b> endpoint: [scheme:][//authority]/wd/hub
         * <p>
         * name: <b>selenium.hub.host</b><br>
         * Selenium 3: <b>http://&lt;{@code localhost}&gt;:4445/wd/hub</b>
         */
        HUB_HOST("selenium.hub.host", null),
        
        /**
         * This is the port assigned to the local <b>Selenium Grid</b> hub server.
         * <p>
         * name: <b>selenium.hub.port</b><br>
         * Selenium 3: <b>4445</b>
         */
        HUB_PORT("selenuim.hub.port", null),
        
        /**
         * This setting specifies a comma-delimited list of fully-qualified names of servlet classes to extend the
         * capabilities of the local <b>Selenium Grid</b> hub server.
         * <p>
         * name: <b>selenium.hub.servlets</b><br>
         * default: {@code null}
         */
        HUB_SERVLETS("selenium.hub.servlets", null),
        
        /**
         * This setting specifies the configuration template name/path for local <b>Selenium Grid</b> node servers.
         * <p>
         * name: <b>selenium.node.config</b><br>
         * Selenium 3: <b>nodeConfig-s3.json</b>
         */
        NODE_CONFIG("selenium.node.config", null),
        
        /**
         * This setting specifies a comma-delimited list of fully-qualified names of servlet classes to extend the
         * capabilities of local <b>Selenium Grid</b> node servers.
         * <p>
         * name: <b>selenium.node.servlets</b><br>
         * default: {@code null}
         */
        NODE_SERVLETS("selenium.node.servlets", null),
        
        /**
         * This setting specifies the browser name or "personality" for new session requests.
         * <p>
         * name: <b>selenium.browser.name</b><br>
         * default: {@code null}
         */
        BROWSER_NAME("selenium.browser.name", null),
        
        /**
         * If {@link #BROWSER_NAME} is undefined, this setting specifies the {@link Capabilities} for new session
         * requests. This can be either a file path (absolute, relative, or simple filename) or a direct value.
         * <p>
         * name: <b>selenium.browser.caps</b><br>
         * default: {@link #DEFAULT_CAPS}
         */
        BROWSER_CAPS("selenium.browser.caps", DEFAULT_CAPS),
        
        /**
         * This setting specifies the maximum allowed interval for a page to finish loading.
         * <p>
         * name: <b>selenium.timeout.pageload</b><br>
         * default: <b>30</b>
         */
        PAGE_LOAD_TIMEOUT("selenium.timeout.pageload", "30"),
        
        /**
         * This setting specifies the maximum amount of time the driver will search for an element.
         * <p>
         * name: <b>selenium.timeout.implied</b><br>
         * default: <b>15</b>
         */
        IMPLIED_TIMEOUT("selenium.timeout.implied", "15"),
        
        /**
         * This setting specifies the maximum allowed interval for an asynchronous script to finish.
         * <p>
         * name: <b>selenium.timeout.script</b><br>
         * default: <b>30</b>
         */
        SCRIPT_TIMEOUT("selenium.timeout.script", "30"),
        
        /**
         * This setting specifies the maximum amount of time to wait for a search context event.
         * <p>
         * name: <b>selenium.timeout.wait</b><br>
         * default: <b>15</b>
         */
        WAIT_TIMEOUT("selenium.timeout.wait", "15"),
        
        /**
         * This setting specifies the maximum amount of time to wait for a Grid server to launch.
         * <p>
         * name: <b>selenium.timeout.host</b><br>
         * default: <b>30</b>
         */
        HOST_TIMEOUT("selenium.timeout.host", "30"),
        
        /**
         * This setting specifies the working directory for local <b>Selenium Grid</b> server processes.
         * <p>
         * name: <b>selenium.grid.working.dir</b><br>
         * default: {@code null}
         */
        GRID_WORKING_DIR("selenium.grid.working.dir", null),
        
        /**
         * This setting specifies the log file folder for local <b>Selenium Grid</b> server processes.
         * <p>
         * <b>NOTE</b>: If a relative path is specified, {@link #GRID_WORKING_DIR} is used as parent. If that's
         * unspecified, the working directory for the current Java process is used (i.e. - {@code user.dir}).
         * <p>
         * name: <b>selenium.grid.log.folder</b><br>
         * default: <b>logs</b>
         */
        GRID_LOGS_FOLDER("selenium.grid.log.folder", "logs"),
        
        /**
         * This setting specifies whether output from local <b>Selenium Grid</b> servers is captured in log files.
         * <p>
         * name: <b>selenium.grid.no.redirect</b><br>
         * default: {@code false}
         */
        GRID_NO_REDIRECT("selenium.grid.no.redirect", "false"),
        
        /**
         * This setting specifies whether the <b>ExamplePageServlet</b> is installed on the hub server of the local
         * <b>Selenium Grid</b> instance. This servlet provides the example page used by the <b>Selenium Foundation</b>
         * unit tests.
         * <p>
         * name: <b>selenium.grid.examples</b><br>
         * default: {@code true}
         */
        GRID_EXAMPLES("selenium.grid.examples", "true"),
        
        /**
         * This setting specifies whether the <b>LifecycleServlet</b> is installed on the hub and node servers of the
         * local <b>Selenium Grid</b> instance. This servlet implements a remote shutdown feature for Grid servers.
         * <p>
         * name: <b>selenium.grid.lifecycle</b><br>
         * default: {@code true}
         */
        GRID_LIFECYCLE("selenium.grid.lifecycle", "true"),
        
        /**
         * This setting specifies the target platform for the current test context.
         * <p>
         * name: <b>selenium.context.platform</b><br>
         * default: {@code null}
         */
        CONTEXT_PLATFORM("selenium.context.platform", null),
        
        /**
         * This setting specifies server arguments passed on to {@code Appium} when it's launched as a local
         * <b>Selenium Grid</b> node server.
         * <p>
         * <b>NOTE</b>: This setting can define multiple {@code Appium} server arguments together, and can be
         * declared multiple times when specified in the <i>settings.properties</i> file.
         * <p>
         * name: <b>appium.cli.args</b><br>
         * default: {@code null}
         */
        APPIUM_CLI_ARGS("appium.cli.args", null),
        
        /**
         * This setting specifies the path to the {@code Appium} main script file.
         * <p>
         * <b>NOTE</b>: If this setting is undefined, <b>Selenium Foundation</b> will check for the main script file in
         * the {@code Appium} package in the global Node package repository.
         * <p>
         * name: <b>appium.binary.path</b><br>
         * default: value of <b>APPIUM_BINARY_PATH</b> environment variable
         */
        APPIUM_BINARY_PATH("appium.binary.path", null),
        
        /**
         * This setting specifies the path to the {@code NodeJS} JavaScript runtime.
         * <p>
         * <b>NOTE</b>: If this setting is unspecified, <b>Selenium Foundation</b> will search for {@code NodeJS} on
         * the System path.
         * <p>
         * name: <b>node.binary.path</b><br>
         * default: value of <b>NODE_BINARY_PATH</b> environment variable
         */
        NODE_BINARY_PATH("node.binary.path", null),
        
        /**
         * This setting specifies the path to the {@code NPM} (Node Package Manager) utility.
         * <p>
         * <b>NOTE</b>: If this setting is unspecified, <b>Selenium Foundation</b> will search for {@code NPM} on the
         * System path.
         * <p>
         * name: <b>npm.binary.path</b><br>
         * default: {@code null}
         */
        NPM_BINARY_PATH("npm.binary.path", null),
        
        /**
         * This setting specifies the path to the {@code PM2} (Process Manager) utility.
         * <p>
         * <b>NOTE</b>: If this setting is unspecified, <b>Selenium Foundation</b> will search for {@code PM2} on the
         * System path.
         * <p>
         * name: <b>pm2.binary.path</b><br>
         * default: {@code null}
         */
        PM2_BINARY_PATH("pm2.binary.path", null),
        
        /**
         * This setting specifies that the {@code Appium} server should be managed by the {@code PM2} utility.
         * <p>
         * <b>NOTE</b>: {@code Appium} requires an active execution context. To run {@code Appium} as a stand-alone
         * <b>Selenium Grid</b> node, the server must to executed as a daemon process. Starting the server via the
         * {@code PM2} utility provides the required persistent execution context.
         * 
         * <p>
         * name: <b>appium.with.pm2</b><br>
         * default: {@code false}
         */
        APPIUM_WITH_PM2("appium.with.pm2", "false");
        
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
         * Get the timeout interval for this wait type.
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
    private URL hubUrl;
    private SeleniumGrid seleniumGrid;
    
    public AbstractSeleniumConfig() throws ConfigurationException, IOException {
        super(SeleniumSettings.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> getDefaults() {
        Map<String, String> defaults = super.getDefaults();
        
        Map<String, String> env = System.getenv();
        String appiumPath = env.get(APPIUM_PATH);
        String nodePath = env.get(NODE_PATH);
        
        if (appiumPath != null) {
            defaults.put(SeleniumSettings.APPIUM_BINARY_PATH.key(), appiumPath);
        }
        
        if (nodePath != null) {
            defaults.put(SeleniumSettings.NODE_BINARY_PATH.key(), nodePath);
        }
        
        return defaults;
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
     * Resolve the specified property name to its value.
     * 
     * <ul>
     *     <li>If the property value refers to an existing resource file, the file contents are returned;</li>
     *     <li>... otherwise, the property value itself is returned (may be {@code null})</li>
     * </ul>
     * 
     * @param propertyName name of the property to resolve
     * @return resolved property value (see description); may be {@code null}
     */
    public String resolveString(String propertyName) {
        String propertyValue = getString(propertyName);
        
        if (propertyValue != null) {
            // try to resolve property value as file path
            String valuePath = getConfigPath(propertyValue);
            
            // if value file found
            if (valuePath != null) {
                try {
                    // load contents of value file
                    Path path = Paths.get(valuePath);
                    URL url = path.toUri().toURL();
                    propertyValue = Resources.toString(url, Charsets.UTF_8);
                } catch (IOException eaten) {
                    // nothing to do here
                }
            }
        }
        return propertyValue;
    }
    
    /**
     * Get the URL for the configured Selenium Grid hub host.
     * 
     * @return {@link URL} for hub host; {@code null} if hub host is unspecified
     */
    public synchronized URL getHubUrl() {
        if (hubUrl == null) {
            String hostStr = getString(SeleniumSettings.HUB_HOST.key());
            if (hostStr != null) {
                try {
                    hubUrl = new URL(hostStr);
                } catch (MalformedURLException e) {
                    throw UncheckedThrow.throwUnchecked(e);
                }
            }
        }
        return hubUrl;
    }
    
    /**
     * Get object that represents the active Selenium Grid.
     * 
     * @return {@link SeleniumGrid} object
     */
    public SeleniumGrid getSeleniumGrid() {
        synchronized(SeleniumGrid.class) {
            if (seleniumGrid == null) {
                try {
                    seleniumGrid = SeleniumGrid.create(getConfig(), getHubUrl());
                } catch (IOException e) {
                    throw UncheckedThrow.throwUnchecked(e);
                }
            }
            return seleniumGrid;
        }
    }
    
    /**
     * Shutdown the active Selenium Grid.
     * 
     * @param localOnly {@code true} to target only local Grid servers
     * @return {@code false} if non-local Grid server encountered; otherwise {@code true}
     * @throws InterruptedException if this thread was interrupted
     */
    public boolean shutdownGrid(final boolean localOnly) throws InterruptedException {
        boolean result = true;
        synchronized(SeleniumGrid.class) {
            if (seleniumGrid != null) {
                result = seleniumGrid.shutdown(localOnly);
                if (result) {
                    seleniumGrid = null;
                }
            }
            return result;
        }
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
            } catch (URISyntaxException eaten) {
                LOGGER.error("Specified target URI '{}' could not be parsed: {}", builder, eaten.getMessage());
            }
        }
        return targetUri;
    }
    
    /**
     * Set the target URI.
     * <p>
     * <b>NOTE</b>: This method also updates the following target URI components: 
     *     {@link SeleniumSettings#TARGET_SCHEME scheme}, {@link SeleniumSettings#TARGET_CREDS credentials},
     *     {@link SeleniumSettings#TARGET_HOST host}, {@link SeleniumSettings#TARGET_PORT port}, and
     *     {@link SeleniumSettings#TARGET_PATH base path}
     * 
     * @param targetUri target URI
     */
    public void setTargetUri(URI targetUri) {
        this.targetUri = targetUri;
        System.setProperty(SeleniumSettings.TARGET_PATH.key(), targetUri.getPath());
        System.setProperty(SeleniumSettings.TARGET_SCHEME.key(), targetUri.getScheme());
        System.setProperty(SeleniumSettings.TARGET_HOST.key(), targetUri.getHost());
        System.setProperty(SeleniumSettings.TARGET_PORT.key(), Integer.toString(targetUri.getPort()));
        String userInfo = targetUri.getUserInfo();
        if (userInfo != null) {
            System.setProperty(SeleniumSettings.TARGET_CREDS.key(), userInfo);
        } else {
            System.clearProperty(SeleniumSettings.TARGET_CREDS.key());
        }
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
     * @throws IllegalArgumentException if {@code BROWSER_NAME} specifies a "personality"
     *         that isn't supported by the active Grid 
     */ 
    public Capabilities getCurrentCapabilities() {
        Capabilities capabilities = null;
        String browserName = getString(SeleniumSettings.BROWSER_NAME.key());
        String browserCaps = resolveString(SeleniumSettings.BROWSER_CAPS.key());
        if (browserName != null) {
            capabilities = getSeleniumGrid().getPersonality(getConfig(), browserName);
        } else if (browserCaps != null) {
            capabilities = getCapabilitiesForJson(browserCaps)[0];
        } else {
            throw new IllegalStateException("Neither browser name nor capabilities are specified");
        }
        
        capabilities = mergeCapabilities(capabilities, getModifications(capabilities, CAPS_MODS_SUFFIX));
        
        String personality = GridUtility.getPersonality(capabilities);
        String defaultCaps = String.format(PERSONALITY, personality);
        
        if ("Espresso".equals(personality)) {
            defaultCaps = APPIUM_HOST +  defaultCaps;
        }
        
        return mergeCapabilities(getCapabilitiesForJson("{" + defaultCaps + "}")[0], capabilities);
    }
    
    /**
     * Get the configured modifier for the specified <b>Capabilities</b> object.
     * <p>
     * <b>NOTE</b>: Modifiers are specified in the configuration as either JSON strings or file paths
     * (absolute, relative, or simple filename). Property names for modifiers correspond to "personality"
     * values within the capabilities themselves (in order of precedence): 
     * 
     * <ul>
     *     <li><b>personality</b>: Selenium Foundation "personality" name</li>
     *     <li><b>automationName</b>: 'appium' automation engine name</li>
     *     <li><b>browserName</b>: Selenium driver browser name</li>
     * </ul>
     * 
     * The first defined value is selected as the "personality" of the specified <b>Capabilities</b> object.
     * The full name of the property used to specify modifiers is the "personality" plus a context-specific
     * suffix: 
     * 
     * <ul>
     *     <li>For node configuration capabilities: <b>&lt;personality&gt;.node.mods</b></li>
     *     <li>For "desired capabilities" requests: <b>&lt;personality&gt;.caps.mods</b></li>
     * </ul>
     * 
     * @param capabilities target capabilities object
     * @param propertySuffix suffix for configuration property name
     * @return configured modifier; {@code null} if none configured
     */
    protected Capabilities getModifications(final Capabilities capabilities, final String propertySuffix) {
        String personality = GridUtility.getPersonality(capabilities);
        if (personality == null) return null;
        
        String propertyName = personality + propertySuffix;
        String modsJson = resolveString(propertyName);
        
        // return mods as [Capabilities] object, or 'null' if none configured
        return (modsJson != null) ? getCapabilitiesForJson(modsJson)[0] : null;
    }
    
    /**
     * Generate a list of browser capabilities objects for the specified name.
     * 
     * @param browserName browser name
     * @return list of {@link Capabilities} objects
     */
    public Capabilities[] getCapabilitiesForName(final String browserName) {
        return getCapabilitiesForJson(String.format(CAPS_PATTERN, browserName));
    }
    
    /**
     * Convert the specified JSON string into a list of browser capabilities objects.
     * 
     * @param capabilities browser capabilities as JSON string
     * @return list of {@link Capabilities} objects
     */
    public abstract Capabilities[] getCapabilitiesForJson(final String capabilities);
    
    /**
     * Apply the indicated modifications to the specified <b>Capabilities</b> object.
     * 
     * @param target target capabilities object
     * @param change revisions being merged (may be {@code null})
     * @return target capabilities object with revisions applied
     */
    public abstract Capabilities mergeCapabilities(final Capabilities target, final Capabilities change);
    
    /**
     * Convert the specified browser capabilities object to a JSON string.
     * 
     * @param capabilities {@link Capabilities} object
     * @return specified capabilities as a JSON string
     */
    public String toJson(final Capabilities capabilities) {
        return toJson(capabilities.asMap());
    }
    
    /**
     * Convert the specified object to a JSON string.
     * 
     * @param obj object to be converted
     * @return specified object as a JSON string
     */
    public abstract String toJson(final Object obj);
    
    /**
     * Get the path to the specified configuration file.
     * 
     * @param path configuration file path (absolute, relative, or simple filename)
     * @return resolved absolute path of specified file; {@code null} if file not found
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
            } catch (URISyntaxException eaten) {
                LOGGER.warn("Invalid URL returned by file locator: {}", eaten.getMessage());
            } catch (IOException eaten) {
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
                FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
            } catch (FileSystemAlreadyExistsException eaten) {
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
    public String[] getDependencyContexts() {
        String gridLauncher = getString(SeleniumSettings.GRID_LAUNCHER.key());
        if (gridLauncher != null) {
            String dependencies = getString(SeleniumSettings.LAUNCHER_DEPS.key());
            if (dependencies != null) {
                return (gridLauncher + File.pathSeparator + dependencies).split(File.pathSeparator);
            } else {
                return new String[] { gridLauncher };
            }
        } else {
            return new String[] {};
        }
    }
    
    /**
     * Create hub configuration file.
     * 
     * @return {@link Path} object for the created (or previously existing) configuration file
     * @throws IOException on failure to create configuration file
     */
    public abstract Path createHubConfig() throws IOException;
    
    /**
     * Create node configuration file from the specified JSON string, to be registered with the indicated hub.
     * 
     * @param capabilities node configuration as JSON string
     * @param hubUrl URL of hub host with which to register
     * @return {@link Path} object for the created (or previously existing) configuration file
     * @throws IOException on failure to create configuration file
     */
    public abstract Path createNodeConfig(String capabilities, URL hubUrl) throws IOException;
    
    /**
     * Get the collection of servlets to install on Selenium Grid hub.
     * 
     * @return collection of specified hub servlets (may be empty)
     */
    public Set<String> getHubServlets() {
        Set<String> servlets = new HashSet<>();
        // get specified hub servlet classes
        String hubServlets = getString(SeleniumSettings.HUB_SERVLETS.key());
        // if servlets are specified
        if ( ! Strings.isNullOrEmpty(hubServlets)) {
            // collect servlet names, minus leading/trailing white space
            servlets.addAll(Arrays.asList(hubServlets.trim().split("\\s*,\\s*")));
        }
        // if example page feature is specified
        if (getBoolean(SeleniumSettings.GRID_EXAMPLES.key())) {
            // add example page servlets to the collection
            servlets.add(ExamplePageServlet.class.getName());
            servlets.add(FrameA_Servlet.class.getName());
            servlets.add(FrameB_Servlet.class.getName());
            servlets.add(FrameC_Servlet.class.getName());
            servlets.add(FrameD_Servlet.class.getName());
        }
        return servlets;
    }
    
    /**
     * Get the collection of servlets to install on Selenium Grid nodes.
     * 
     * @return collection of specified node servlets (may be empty)
     */
    public Set<String> getNodeServlets() {
        Set<String> servlets = new HashSet<>();
        // get specified node servlet classes
        String nodeServlets = getString(SeleniumSettings.NODE_SERVLETS.key());
        // if servlets are specified
        if ( ! Strings.isNullOrEmpty(nodeServlets)) {
            // collect servlet names, minus leading/trailing white space
            servlets.addAll(Arrays.asList(nodeServlets.trim().split("\\s*,\\s*")));
        }
        // if remote shutdown feature is specified
        if (getBoolean(SeleniumSettings.GRID_LIFECYCLE.key())) {
            // add lifecycle servlet to the collection
            servlets.add(LifecycleServlet.class.getName());
        }
        return servlets;
    }
    
    /**
     * Get the target platform for the current test context.
     * 
     * @return target platform for the current test context
     */
    public String getContextPlatform() {
        return getConfig().getString(SeleniumSettings.CONTEXT_PLATFORM.key());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getSettingsPath() {
        return SETTINGS_FILE;
    }
}
