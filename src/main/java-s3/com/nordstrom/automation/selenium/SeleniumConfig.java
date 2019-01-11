package com.nordstrom.automation.selenium;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.openqa.grid.internal.utils.configuration.GridNodeConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.JsonInput;

import com.nordstrom.automation.settings.SettingsCore;

/**
 * This class declares settings and methods related to WebDriver and Grid configuration specific to the Selenium 3 API.
 * 
 * @see SettingsCore
 */
@SuppressWarnings({"squid:S1200", "squid:S2972", "squid:MaximumInheritanceDepth"})
public class SeleniumConfig extends AbstractSeleniumConfig {
    
    private static final String JSON_HEAD = "{ \"capabilities\": [";
    private static final String JSON_TAIL = "] }";
    private static final String GRID_LAUNCHER = "org.openqa.grid.selenium.GridLauncherV3";
    private static final String HUB_PORT = "4445";
    private static final String NODE_CONFIG = "nodeConfig-s3.json";
    
    /**
     * <b>org.openqa.grid.selenium.GridLauncherV3</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-server-standalone&lt;/artifactId&gt;
     *  &lt;version&gt;3.14.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.openqa.selenium.phantomjs.PhantomJSDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.github.detro&lt;/groupId&gt;
     *  &lt;artifactId&gt;ghostdriver&lt;/artifactId&gt;
     *  &lt;version&gt;2.1.0&lt;/version&gt;
     *  &lt;exclusions&gt;
     *    &lt;exclusion&gt;
     *      &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *      &lt;artifactId&gt;selenium-remote-driver&lt;/artifactId&gt;
     *    &lt;/exclusion&gt;
     *  &lt;/exclusions&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
                    GRID_LAUNCHER,
                    "org.openqa.selenium.BuildInfo",
                    "com.google.common.collect.ImmutableMap",
                    "com.beust.jcommander.JCommander",
                    "org.openqa.selenium.json.Json",
                    "org.seleniumhq.jetty9.util.thread.ThreadPool",
                    "javax.servlet.Servlet",
                    "okhttp3.ConnectionPool",
                    "okio.BufferedSource"
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
        defaults.put(SeleniumSettings.GRID_LAUNCHER.key(), GRID_LAUNCHER);
        defaults.put(SeleniumSettings.HUB_PORT.key(), HUB_PORT);
        defaults.put(SeleniumSettings.NODE_CONFIG.key(), NODE_CONFIG);
        return defaults;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getDependencyContexts() {
        return DEPENDENCY_CONTEXTS;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Path createNodeConfig(String capabilities, URL hubUrl) throws IOException {
        String nodeConfigPath = getNodeConfigPath().toString();
        String[] configPathBits = nodeConfigPath.split("\\.");
        String hashCode = String.format("%08X", capabilities.hashCode());
        Path filePath = Paths.get(configPathBits[0] + "-" + hashCode + "." + configPathBits[1]);
        if (filePath.toFile().exists()) {
            Files.delete(filePath);
        }
        if (filePath.toFile().createNewFile()) {
            JsonInput input = new Json().newInput(new StringReader(JSON_HEAD + capabilities + JSON_TAIL));
            List<MutableCapabilities> capabilitiesList = GridNodeConfiguration.loadFromJSON(input).capabilities;
            GridNodeConfiguration nodeConfig = GridNodeConfiguration.loadFromJSON(nodeConfigPath);
            nodeConfig.capabilities = capabilitiesList;
            nodeConfig.hub = hubUrl.toString();
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(filePath.toFile()))) {
                out.write(new Json().toJson(nodeConfig).getBytes(StandardCharsets.UTF_8));
            }
        }
        return filePath;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Capabilities[] getCapabilitiesForJson(String capabilities) {
        JsonInput input = new Json().newInput(new StringReader(JSON_HEAD + capabilities + JSON_TAIL));
        return GridNodeConfiguration.loadFromJSON(input).capabilities.stream().toArray(Capabilities[]::new);
    }
    
    /**
     * {@inheritDoc}
     */
    public String toJson(Capabilities capabilities) {
        return new Json().toJson(capabilities);
    }
}
