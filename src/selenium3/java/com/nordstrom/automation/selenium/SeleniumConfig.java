package com.nordstrom.automation.selenium;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.openqa.grid.internal.utils.CapabilityMatcher;
import org.openqa.grid.internal.utils.configuration.GridHubConfiguration;
import org.openqa.grid.internal.utils.configuration.GridNodeConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.grid.config.ConfigException;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.JsonInput;

import com.nordstrom.automation.settings.SettingsCore;

/**
 * This class declares settings and methods related to WebDriver and Grid configuration specific to the Selenium 3 API.
 * 
 * @see SettingsCore
 */
public class SeleniumConfig extends AbstractSeleniumConfig {
    
    private static final String JSON_HEAD = "{ \"capabilities\": [";
    private static final String JSON_TAIL = "] }";
    private static final String DEFAULT_GRID_LAUNCHER = "org.openqa.grid.selenium.GridLauncherV3";
    private static final String DEFAULT_HUB_PORT = "4445";
    private static final String DEFAULT_HUB_CONFIG = "hubConfig-s3.json";
    private static final String DEFAULT_NODE_CONFIG = "nodeConfig-s3.json";
    
    /**
     * <b>org.openqa.grid.selenium.GridLauncherV3</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-server&lt;/artifactId&gt;
     *  &lt;version&gt;3.141.59&lt;/version&gt;
     *  &lt;exclusions&gt;
     *    &lt;exclusion&gt;
     *      &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *      &lt;artifactId&gt;selenium-chrome-driver&lt;/artifactId&gt;
     *    &lt;/exclusion&gt;
     *    &lt;exclusion&gt;
     *      &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *      &lt;artifactId&gt;selenium-edge-driver&lt;/artifactId&gt;
     *    &lt;/exclusion&gt;
     *    &lt;exclusion&gt;
     *      &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *      &lt;artifactId&gt;selenium-firefox-driver&lt;/artifactId&gt;
     *    &lt;/exclusion&gt;
     *    &lt;exclusion&gt;
     *      &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *      &lt;artifactId&gt;selenium-ie-driver&lt;/artifactId&gt;
     *    &lt;/exclusion&gt;
     *    &lt;exclusion&gt;
     *      &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *      &lt;artifactId&gt;selenium-opera-driver&lt;/artifactId&gt;
     *    &lt;/exclusion&gt;
     *    &lt;exclusion&gt;
     *      &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *      &lt;artifactId&gt;selenium-java&lt;/artifactId&gt;
     *    &lt;/exclusion&gt;
     *    &lt;exclusion&gt;
     *      &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *      &lt;artifactId&gt;selenium-safari-driver&lt;/artifactId&gt;
     *    &lt;/exclusion&gt;
     *    &lt;exclusion&gt;
     *      &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *      &lt;artifactId&gt;htmlunit-driver&lt;/artifactId&gt;
     *    &lt;/exclusion&gt;
     *    &lt;exclusion&gt;
     *      &lt;groupId&gt;net.sourceforge.htmlunit&lt;/groupId&gt;
     *      &lt;artifactId&gt;htmlunit&lt;/artifactId&gt;
     *    &lt;/exclusion&gt;
     *  &lt;/exclusions&gt;
     *&lt;/dependency&gt;</pre>
     *
     * <b>org.openqa.selenium.BuildInfo</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-api&lt;/artifactId&gt;
     *  &lt;version&gt;3.141.59&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     *
     * <b>com.google.common.collect.ImmutableMap</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.google.guava&lt;/groupId&gt;
     *  &lt;artifactId&gt;guava&lt;/artifactId&gt;
     *  &lt;version&gt;25.0-jre&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     *
     * <b>com.beust.jcommander.JCommander</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.beust&lt;/groupId&gt;
     *  &lt;artifactId&gt;jcommander&lt;/artifactId&gt;
     *  &lt;version&gt;1.72&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     *
     * <b>org.openqa.selenium.json.Json</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-remote-driver&lt;/artifactId&gt;
     *  &lt;version&gt;3.141.59&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     *
     * <b>org.seleniumhq.jetty9.util.thread.ThreadPool</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;jetty-repacked&lt;/artifactId&gt;
     *  &lt;version&gt;9.4.12v20180830&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     *
     * <b>javax.servlet.Servlet</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;javax.servlet&lt;/groupId&gt;
     *  &lt;artifactId&gt;javax-servlet-api&lt;/artifactId&gt;
     *  &lt;version&gt;3.1.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     *
     * <b>okhttp3.ConnectionPool</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.squareup.okhttp3&lt;/groupId&gt;
     *  &lt;artifactId&gt;okhttp3&lt;/artifactId&gt;
     *  &lt;version&gt;3.11.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     *
     * <b>okio.BufferedSource</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.squareup.okio&lt;/groupId&gt;
     *  &lt;artifactId&gt;okio&lt;/artifactId&gt;
     *  &lt;version&gt;1.14.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>ch.qos.logback.classic.spi.ThrowableProxy</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;ch.qos.logback&lt;/groupId&gt;
     *  &lt;artifactId&gt;logback-classic&lt;/artifactId&gt;
     *  &lt;version&gt;1.2.3&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     *
     * <b>kotlin.jvm.internal.Intrinsics</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.jetbrains.kotlin&lt;/groupId&gt;
     *  &lt;artifactId&gt;kotlin-stdlib&lt;/artifactId&gt;
     *  &lt;version&gt;1.4.10&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = { "com.nordstrom.automation.selenium.core.LocalSeleniumGrid",
            "com.nordstrom.common.file.PathUtils", "org.apache.commons.lang3.reflect.FieldUtils",
            "net.bytebuddy.matcher.ElementMatcher", "org.openqa.selenium.BuildInfo",
            "com.google.common.collect.ImmutableMap", "com.beust.jcommander.JCommander",
            "org.openqa.selenium.json.Json", "org.seleniumhq.jetty9.util.thread.ThreadPool", "javax.servlet.Servlet",
            "okhttp3.ConnectionPool", "okio.BufferedSource", "ch.qos.logback.classic.spi.ThrowableProxy",
            "kotlin.jvm.internal.Intrinsics", "org.apache.commons.exec.Executor" };
    
    static {
        try {
            seleniumConfig = new SeleniumConfig();
        } catch (ConfigurationException | IOException e) {
            throw new RuntimeException("Failed to instantiate settings", e);
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
    public int getVersion() {
        return 3;
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
        // create hub configuration from template
        GridHubConfiguration hubConfig = GridHubConfiguration.loadFromJSON(hubConfigPath);
        
        String slotMatcher = getString(SeleniumSettings.SLOT_MATCHER.key());
        
        // get configured grid servlet collection
        Set<String> servlets = getGridServlets();
        // merge with hub template servlets
        servlets.addAll(hubConfig.servlets);
        
        // strip extension to get template base path
        String configPathBase = hubConfigPath.substring(0, hubConfigPath.length() - 5);
        // get hash code of slot matcher and servlets as 8-digit hexadecimal string
        String hashCode = String.format("%08X", Objects.hash(slotMatcher, servlets));
        // assemble hub configuration file path with servlets hash code
        Path filePath = Paths.get(configPathBase + "-" + hashCode + ".json");
        
        // if assembled path does not exist
        if (filePath.toFile().createNewFile()) {
            try {
                hubConfig.capabilityMatcher = (CapabilityMatcher) Class.forName(slotMatcher)
                        .getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new ConfigException("Failed instantiating capability matcher: " + slotMatcher, e);
            }
            hubConfig.servlets = Arrays.asList(servlets.toArray(new String[0]));
            try(OutputStream fos = new FileOutputStream(filePath.toFile());
                OutputStream out = new BufferedOutputStream(fos)) {
                out.write(new Json().toJson(hubConfig).getBytes(UTF_8));
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
        // create node configuration from template
        GridNodeConfiguration nodeConfig = GridNodeConfiguration.loadFromJSON(nodeConfigPath);
        
        // convert capabilities string to [JsonInput] object
        JsonInput input = new Json().newInput(new StringReader(JSON_HEAD + capabilities + JSON_TAIL));
        // convert [JsonInput] object to list of [MutableCapabilities] objects
        List<MutableCapabilities> capabilitiesList = GridNodeConfiguration.loadFromJSON(input).capabilities;
        // for each [MutableCapabilities] object
        for (MutableCapabilities theseCaps : capabilitiesList) {
            // apply specified node modifications (if any)
            theseCaps.merge(getModifications(theseCaps, NODE_MODS_SUFFIX));
        }
        
        // convert list of [MutableCapabilities] objects to set of maps
        Set<Map<String, Object>> capabilitiesSet = capabilitiesList.stream()
                .map(caps -> caps.toJson())
                .collect(Collectors.toSet());
        
        // get configured node servlet collection
        Set<String> servlets = new HashSet<>(nodeConfig.servlets);
        
        // strip extension to get template base path
        String configPathBase = nodeConfigPath.substring(0, nodeConfigPath.length() - 5);
        // get hash code of capabilities set, hub URL, and servlets as 8-digit hexadecimal string
        String hashCode = String.format("%08X", Objects.hash(capabilitiesSet, hubUrl, servlets));
        // assemble node configuration file path with aggregated hash code
        Path filePath = Paths.get(configPathBase + "-" + hashCode + ".json");
        
        // if assembled path does not exist
        if (filePath.toFile().createNewFile()) {
            nodeConfig.hub = null;
            nodeConfig.capabilities = capabilitiesList;
            nodeConfig.hubHost = hubUrl.getHost();
            nodeConfig.hubPort = hubUrl.getPort();
            nodeConfig.servlets = Arrays.asList(servlets.toArray(new String[0]));
            try(OutputStream fos = new FileOutputStream(filePath.toFile());
                OutputStream out = new BufferedOutputStream(fos)) {
                out.write(new Json().toJson(nodeConfig).getBytes(UTF_8));
            }
        }
        return filePath;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Path createRelayConfig(String capabilities, URL hubUrl) throws IOException {
        throw new UnsupportedOperationException("Relay nodes are unsupported prior to Selenium 4");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Capabilities[] getCapabilitiesForJson(String capabilities) {
        JsonInput input = new Json().newInput(new StringReader(JSON_HEAD + capabilities + JSON_TAIL));
        return GridNodeConfiguration.loadFromJSON(input).capabilities.toArray(new Capabilities[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Capabilities mergeCapabilities(Capabilities target, Capabilities change) {
        return target.merge(change);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toJson(Object obj) {
        return new Json().toJson(obj);
    }
}
