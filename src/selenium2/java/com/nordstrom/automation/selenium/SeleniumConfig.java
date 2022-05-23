package com.nordstrom.automation.selenium;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.openqa.grid.common.GridRole;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nordstrom.automation.settings.SettingsCore;

/**
 * This class declares settings and methods related to WebDriver and Grid configuration specific to the Selenium 2 API.
 * 
 * @see SettingsCore
 */
public class SeleniumConfig extends AbstractSeleniumConfig {
    
    private static final String JSON_HEAD = "{ \"capabilities\": [";
    private static final String JSON_TAIL = "], \"configuration\": {} }";
    private static final String DEFAULT_GRID_LAUNCHER = "org.openqa.grid.selenium.GridLauncher";
    private static final String DEFAULT_HUB_PORT = "4444";
    private static final String DEFAULT_HUB_CONFIG = "hubConfig-s2.json";
    private static final String DEFAULT_NODE_CONFIG = "nodeConfig-s2.json";
    
    /**
     * <b>org.openqa.grid.selenium.GridLauncher</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-server&lt;/artifactId&gt;
     *  &lt;version&gt;2.53.1&lt;/version&gt;
     *  &lt;exclusions&gt;
     *    &lt;exclusion&gt;
     *      &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *      &lt;artifactId&gt;selenium-java&lt;/artifactId&gt;
     *    &lt;/exclusion&gt;
     *  &lt;/exclusions&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>com.google.common.util.concurrent.SimpleTimeLimiter</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.google.guava&lt;/groupId&gt;
     *  &lt;artifactId&gt;guava&lt;/artifactId&gt;
     *  &lt;version&gt;21.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>com.google.gson.JsonIOException</b> (for selenium-remote-driver)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.google.code.gson&lt;/groupId&gt;
     *  &lt;artifactId&gt;gson&lt;/artifactId&gt;
     *  &lt;version&gt;2.3.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.openqa.selenium.remote.JsonToBeanConverter</b> (for selenium-support)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-remote-driver&lt;/artifactId&gt;
     *  &lt;version&gt;2.53.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.openqa.selenium.WebDriverException</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-api&lt;/artifactId&gt;
     *  &lt;version&gt;2.53.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.seleniumhq.jetty9.util.Jetty</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;jetty-repacked&lt;/artifactId&gt;
     *  &lt;version&gt;9.2.13.v20150730&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.http.conn.HttpClientConnectionManager</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.apache.httpcomponents&lt;/groupId&gt;
     *  &lt;artifactId&gt;httpclient&lt;/artifactId&gt;
     *  &lt;version&gt;4.5.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.http.config.RegistryBuilder</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.apache.httpcomponents&lt;/groupId&gt;
     *  &lt;artifactId&gt;httpcore&lt;/artifactId&gt;
     *  &lt;version&gt;4.4.3&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.commons.logging.LogFactory</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;commons-logging&lt;/groupId&gt;
     *  &lt;artifactId&gt;commons-logging&lt;/artifactId&gt;
     *  &lt;version&gt;1.2&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>javax.servlet.http.HttpServlet</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;javax.servlet&lt;/groupId&gt;
     *  &lt;artifactId&gt;javax.servlet-api&lt;/artifactId&gt;
     *  &lt;version&gt;3.1.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.openqa.jetty.util.MultiException</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;jetty-rc-repacked&lt;/artifactId&gt;
     *  &lt;version&gt;5&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.openqa.selenium.support.events.WebDriverEventListener</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-support&lt;/artifactId&gt;
     *  &lt;version&gt;2.53.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>ch.qos.logback.classic.spi.ThrowableProxy</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;ch.qos.logback&lt;/groupId&gt;
     *  &lt;artifactId&gt;logback-classic&lt;/artifactId&gt;
     *  &lt;version&gt;1.2.3&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
                    "com.nordstrom.tools.GuavaAgent",           // guava-agent
                    "net.bytebuddy.matcher.ElementMatcher",     // guava-agent
                    "com.nordstrom.automation.selenium.servlet.ExamplePageServlet",
                    "com.google.common.util.concurrent.SimpleTimeLimiter",
                    "com.google.gson.JsonIOException",
                    "org.openqa.selenium.remote.JsonToBeanConverter",
                    "org.openqa.selenium.WebDriverException",
                    "org.seleniumhq.jetty9.util.Jetty",
                    "org.apache.http.conn.HttpClientConnectionManager",
                    "org.apache.http.config.RegistryBuilder",
                    "org.apache.commons.logging.LogFactory",
                    "javax.servlet.http.HttpServlet",
                    "org.openqa.jetty.util.MultiException",
                    "org.openqa.selenium.support.events.WebDriverEventListener",
                    "ch.qos.logback.classic.spi.ThrowableProxy",
                    "org.apache.commons.exec.Executor"
                    };
    
    static {
        try {
            seleniumConfig = new SeleniumConfig();
        } catch (ConfigurationException | IOException e) {
            throw new RuntimeException("Failed to instantiate settings", e); //NOSONAR
        }
    }
    
    /**
     * Instantiate a <b>Selenium Foundation</b> configuration object.
     * 
     * @throws ConfigurationException If a failure is encountered while initializing this configuration object.
     * @throws IOException If a failure is encountered while reading from a configuration input stream.
     */
    public SeleniumConfig() throws ConfigurationException, IOException {
        super();
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
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> getDefaults() {
        Map<String, String> defaults = super.getDefaults();
        defaults.put(SeleniumSettings.GRID_LAUNCHER.key(), DEFAULT_GRID_LAUNCHER);
        defaults.put(SeleniumSettings.LAUNCHER_DEPS.key(), StringUtils.join(DEPENDENCY_CONTEXTS, File.pathSeparator));
        defaults.put(SeleniumSettings.HUB_PORT.key(), DEFAULT_HUB_PORT);
        defaults.put(SeleniumSettings.HUB_CONFIG.key(), DEFAULT_HUB_CONFIG);
        defaults.put(SeleniumSettings.NODE_CONFIG.key(), DEFAULT_NODE_CONFIG);
        return defaults;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Path createHubConfig() throws IOException {
        // get path to hub configuration template
        String hubConfigPath = getHubConfigPath().toString();
        // create empty registration request
        GridHubConfiguration hubConfig = new GridHubConfiguration();
        // populate request from hub template
        hubConfig.loadFromJSON(hubConfigPath);
        
        // get configured hub servlet collection
        Set<String> hubServlets = getHubServlets();
        // get servlet specification from hub template
        List<String> servlets = hubConfig.getServlets();
        // if hub template specifies servlets
        if ( ! ((servlets == null) || servlets.isEmpty())) {
            // merge hub template specification with configured servlets
            hubServlets.addAll(servlets);
        }
        
        // strip extension to get template base path
        String configPathBase = hubConfigPath.substring(0, hubConfigPath.length() - 5);
        // get hash code of servlets as 8-digit hexadecimal string
        String hashCode = String.format("%08X", hubServlets.hashCode());
        // assemble hub configuration file path with servlets hash code
        Path filePath = Paths.get(configPathBase + "-" + hashCode + ".json");
        
        // if assembled path does not exist
        if (filePath.toFile().createNewFile()) {
            // if servlets are specified
            if ( ! hubServlets.isEmpty()) {
                // set registration request servlet specification
                hubConfig.setServlets(Arrays.asList(hubServlets.toArray(new String[0])));
            }
            
            try(OutputStream fos = new FileOutputStream(filePath.toFile());
                OutputStream out = new BufferedOutputStream(fos)) {
                out.write(toJSON(hubConfig).getBytes(StandardCharsets.UTF_8));
            }
        }
        return filePath;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Path createNodeConfig(String capabilities, URL hubUrl) throws IOException {
        // get path to node configuration template
        String nodeConfigPath = getNodeConfigPath().toString();
        // create empty registration request
        RegistrationRequest nodeConfig = new RegistrationRequest();
        // populate request from node template
        nodeConfig.loadFromJSON(nodeConfigPath);
        
        // assemble complete JSON registration request
        String input = JSON_HEAD + capabilities + JSON_TAIL;
        // convert JSON registration request to list of [DesiredCapabilities] objects
        List<DesiredCapabilities> capabilitiesList = RegistrationRequest.getNewInstance(input).getCapabilities();
        // for each [DesiredCapabilities] object
        for (DesiredCapabilities theseCaps : capabilitiesList) {
            // apply specified node modifications (if any)
            theseCaps.merge(getModifications(theseCaps, NODE_MODS_SUFFIX));
        }
        
        // get configured node servlet collection
        Set<String> nodeServlets = getNodeServlets();
        // get servlet specification from node template
        String servlets = nodeConfig.getConfigAsString(RegistrationRequest.SERVLETS);
        // if node template specifies servlets
        if ( ! Strings.isNullOrEmpty(servlets)) {
            // merge node template specification with configured servlets
            nodeServlets.addAll(Arrays.asList(servlets.trim().split("\\s*,\\s*")));
        }
        
        // strip extension to get template base path
        String configPathBase = nodeConfigPath.substring(0, nodeConfigPath.length() - 5);
        // get hash code of capabilities list, hub URL, and servlets as 8-digit hexadecimal string
        String hashCode = String.format("%08X", Objects.hash(capabilitiesList, hubUrl, nodeServlets));
        // assemble node configuration file path with aggregated hash code
        Path filePath = Paths.get(configPathBase + "-" + hashCode + ".json");
        
        // if assembled path does not exist
        if (filePath.toFile().createNewFile()) {
            // set registration request capabilities
            nodeConfig.setCapabilities(capabilitiesList);
            
            // get registration request configuration
            Map<String, Object> configuration = nodeConfig.getConfiguration();
            
            // set registration request hub host and port
            configuration.put(RegistrationRequest.HUB_HOST, hubUrl.getHost());
            configuration.put(RegistrationRequest.HUB_PORT, hubUrl.getPort());

            // if servlets are specified
            if ( ! nodeServlets.isEmpty()) {
                // set registration request servlet specification
                configuration.put(RegistrationRequest.SERVLETS, Joiner.on(",").join(nodeServlets));
            }
          
            // hack for RegistrationRequest bug
            nodeConfig.setRole(GridRole.NODE);
            
            try(OutputStream fos = new FileOutputStream(filePath.toFile());
                OutputStream out = new BufferedOutputStream(fos)) {
                out.write(nodeConfig.toJSON().getBytes(StandardCharsets.UTF_8));
            }
        }
        return filePath;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Capabilities[] getCapabilitiesForJson(String capabilities) {
        String input = JSON_HEAD + capabilities + JSON_TAIL;
        return RegistrationRequest.getNewInstance(input).getCapabilities().toArray(new Capabilities[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Capabilities mergeCapabilities(Capabilities target, Capabilities change) {
        return ((DesiredCapabilities) target).merge(change);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toJson(Object obj) {
        return new Gson().toJsonTree(obj).toString();
    }
    
    /**
     * Convert the specified {@link GridHubConfiguration} object to a JSON string.
     * 
     * @param hubConfig grid hub configuration object
     * @return grid hub configuration as a JSON string
     */
    public String toJSON(GridHubConfiguration hubConfig) {
        return new Gson().toJson(getAssociatedJSON(hubConfig));
    }

    /**
     * Get the specified {@link GridHubConfiguration} object as a {@link JsonObject}.
     * 
     * @param hubConfig grid hub configuration object
     * @return JsonObject equivalent of grid hub configuration
     */
    public JsonObject getAssociatedJSON(GridHubConfiguration hubConfig) {
        JsonObject res = new JsonObject();
        Map<String, Object> allParams = hubConfig.getAllParams();

        res.addProperty("class", hubConfig.getClass().getCanonicalName());
        res.addProperty("role", "hub");
        res.addProperty("host", hubConfig.getHost());
        res.addProperty("port", hubConfig.getPort());
        res.addProperty("newSessionWaitTimeout", hubConfig.getNewSessionWaitTimeout());
        res.add("servlets", new Gson().toJsonTree(hubConfig.getServlets()));
        if (hubConfig.getPrioritizer() != null) {
            res.addProperty("prioritizer", hubConfig.getPrioritizer().getClass().getCanonicalName());
        }
        if (hubConfig.getCapabilityMatcher() != null) {
            res.addProperty("capabilityMatcher", hubConfig.getCapabilityMatcher().getClass().getCanonicalName());
        }
        res.addProperty("throwOnCapabilityNotPresent", hubConfig.isThrowOnCapabilityNotPresent());
        if (allParams.get("nodePolling") != null) {
            res.addProperty("nodePolling", (Integer) allParams.get("nodePolling"));
        }
        res.addProperty("cleanUpCycle", hubConfig.getCleanupCycle());
        res.addProperty("timeout", hubConfig.getTimeout());
        res.addProperty("browserTimeout", hubConfig.getBrowserTimeout());
        res.addProperty("maxSession", (Integer) allParams.get("maxSession"));
        res.addProperty("jettyMaxThreads", hubConfig.getJettyMaxThreads());
        res.addProperty("debug", (Boolean) allParams.get("debug"));

        return res;
    }
}
