package com.nordstrom.automation.selenium;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.openqa.selenium.json.Json.LIST_OF_MAPS_TYPE;
import static org.openqa.selenium.json.Json.MAP_TYPE;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.grid.config.ConfigException;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.net.PortProber;

import com.nordstrom.automation.selenium.core.GridServer;
import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.utility.HostUtils;
import com.nordstrom.automation.settings.SettingsCore;

/**
 * This class declares settings and methods related to WebDriver and Grid configuration specific to the Selenium 3 API.
 * 
 * @see SettingsCore
 */
public class SeleniumConfig extends AbstractSeleniumConfig {
    
    private static final String DEFAULT_GRID_LAUNCHER = "org.openqa.selenium.grid.Bootstrap";
    private static final String DEFAULT_HUB_PORT = "4446";
    private static final String DEFAULT_HUB_CONFIG = "hubConfig-s4.json";
    private static final String DEFAULT_NODE_CONFIG = "nodeConfig-s4.json";
    
    /**
     * <b>org.openqa.selenium.grid.Main</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-grid&lt;/artifactId&gt;
     *  &lt;version&gt;4.9.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.openqa.selenium.io.Zip</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-remote-driver&lt;/artifactId&gt;
     *  &lt;version&gt;4.9.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.openqa.selenium.remote.http.Route</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-http&lt;/artifactId&gt;
     *  &lt;version&gt;4.9.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.openqa.selenium.net.Urls</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-api&lt;/artifactId&gt;
     *  &lt;version&gt;4.9.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.openqa.selenium.json.Json</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-json&lt;/artifactId&gt;
     *  &lt;version&gt;4.9.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>com.google.common.base.Utf8</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.google.guava&lt;/groupId&gt;
     *  &lt;artifactId&gt;guava&lt;/artifactId&gt;
     *  &lt;version&gt;31.1-jre&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>com.beust.jcommander.Strings</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.beust&lt;/groupId&gt;
     *  &lt;artifactId&gt;jcommander&lt;/artifactId&gt;
     *  &lt;version&gt;1.82&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.opentelemetry.sdk.autoconfigure.SpiUtil</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.opentelemetry&lt;/groupId&gt;
     *  &lt;artifactId&gt;opentelemetry-sdk-extension-autoconfigure&lt;/artifactId&gt;
     *  &lt;version&gt;1.25.0-alpha&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.opentelemetry.sdk.autoconfigure.spi.Ordered</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.opentelemetry&lt;/groupId&gt;
     *  &lt;artifactId&gt;opentelemetry-sdk-extension-autoconfigure-spi&lt;/artifactId&gt;
     *  &lt;version&gt;1.25.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.opentelemetry.api.trace.Span</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.opentelemetry&lt;/groupId&gt;
     *  &lt;artifactId&gt;opentelemetry-api&lt;/artifactId&gt;
     *  &lt;version&gt;1.25.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.opentelemetry.api.logs.Logger</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.opentelemetry&lt;/groupId&gt;
     *  &lt;artifactId&gt;opentelemetry-api-logs&lt;/artifactId&gt;
     *  &lt;version&gt;1.25.0-alpha&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.opentelemetry.api.events.EventEmitter</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.opentelemetry&lt;/groupId&gt;
     *  &lt;artifactId&gt;opentelemetry-api-events&lt;/artifactId&gt;
     *  &lt;version&gt;1.25.0-alpha&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.opentelemetry.sdk.trace.SdkSpan</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.opentelemetry&lt;/groupId&gt;
     *  &lt;artifactId&gt;opentelemetry-sdk-trace&lt;/artifactId&gt;
     *  &lt;version&gt;1.25.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.opentelemetry.context.Scope</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.opentelemetry&lt;/groupId&gt;
     *  &lt;artifactId&gt;opentelemetry-context&lt;/artifactId&gt;
     *  &lt;version&gt;1.25.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.opentelemetry.sdk.metrics.View</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.opentelemetry&lt;/groupId&gt;
     *  &lt;artifactId&gt;opentelemetry-sdk-metrics&lt;/artifactId&gt;
     *  &lt;version&gt;1.25.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre></pre>
     * 
     * <b>io.opentelemetry.sdk.logs.LogLimits</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.opentelemetry&lt;/groupId&gt;
     *  &lt;artifactId&gt;opentelemetry-sdk-logs&lt;/artifactId&gt;
     *  &lt;version&gt;1.25.0-alpha&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.opentelemetry.sdk.common.Clock</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.opentelemetry&lt;/groupId&gt;
     *  &lt;artifactId&gt;opentelemetry-sdk-common&lt;/artifactId&gt;
     *  &lt;version&gt;1.25.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.opentelemetry.semconv.trace.attributes.SemanticAttributes</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.opentelemetry&lt;/groupId&gt;
     *  &lt;artifactId&gt;opentelemetry-semconv&lt;/artifactId&gt;
     *  &lt;version&gt;1.25.0-alpha&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.opentelemetry.sdk.OpenTelemetrySdk</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.opentelemetry&lt;/groupId&gt;
     *  &lt;artifactId&gt;opentelemetry-sdk&lt;/artifactId&gt;
     *  &lt;version&gt;1.25.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.zeromq.Utils</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.zeromq&lt;/groupId&gt;
     *  &lt;artifactId&gt;jeromq&lt;/artifactId&gt;
     *  &lt;version&gt;0.5.3&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>dev.failsafe.Call</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;dev.failsafe&lt;/groupId&gt;
     *  &lt;artifactId&gt;failsafe&lt;/artifactId&gt;
     *  &lt;version&gt;3.3.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>graphql.Assert</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.graphql-java&lt;/groupId&gt;
     *  &lt;artifactId&gt;graphql-java&lt;/artifactId&gt;
     *  &lt;version&gt;20.2&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.dataloader.DataLoader</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.graphql-java&lt;/groupId&gt;
     *  &lt;artifactId&gt;java-dataloader&lt;/artifactId&gt;
     *  &lt;version&gt;3.2.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.slf4j.MDC</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.slf4j&lt;/groupId&gt;
     *  &lt;artifactId&gt;slf4j-api&lt;/artifactId&gt;
     *  &lt;version&gt;1.7.36&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.netty.channel.Channel</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.netty&lt;/groupId&gt;
     *  &lt;artifactId&gt;netty-transport&lt;/artifactId&gt;
     *  &lt;version&gt;4.2.0.Final&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.netty.util.Timer</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.netty&lt;/groupId&gt;
     *  &lt;artifactId&gt;netty-common&lt;/artifactId&gt;
     *  &lt;version&gt;4.2.0.Final&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.netty.handler.ssl.SslUtils</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.netty&lt;/groupId&gt;
     *  &lt;artifactId&gt;netty-handler&lt;/artifactId&gt;
     *  &lt;version&gt;4.2.0.Final&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.slf4j.impl.StaticLoggerBinder</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;ch.qos.logback&lt;/groupId&gt;
     *  &lt;artifactId&gt;logback-classic&lt;/artifactId&gt;
     *  &lt;version&gt;1.2.11&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>ch.qos.logback.core.Layout</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;ch.qos.logback&lt;/groupId&gt;
     *  &lt;artifactId&gt;logback-core&lt;/artifactId&gt;
     *  &lt;version&gt;1.2.11&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.netty.buffer.ByteBuf</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.netty&lt;/groupId&gt;
     *  &lt;artifactId&gt;netty-buffer&lt;/artifactId&gt;
     *  &lt;version&gt;4.2.0.Final&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.netty.handler.codec.http.Cookie</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.netty&lt;/groupId&gt;
     *  &lt;artifactId&gt;netty-codec-http&lt;/artifactId&gt;
     *  &lt;version&gt;4.2.0.Final&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.netty.handler.codec.Headers</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.netty&lt;/groupId&gt;
     *  &lt;artifactId&gt;netty-codec&lt;/artifactId&gt;
     *  &lt;version&gt;4.2.0.Final&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>com.google.common.util.concurrent.internal.InternalFutures</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.google.guava&lt;/groupId&gt;
     *  &lt;artifactId&gt;failureaccess&lt;/artifactId&gt;
     *  &lt;version&gt;1.0.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>io.netty.handler.codec.compression.ZlibCodecFactory</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.netty&lt;/groupId&gt;
     *  &lt;artifactId&gt;netty-codec-compression&lt;/artifactId&gt;
     *  &lt;version&gt;4.2.0.Final&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = { "com.nordstrom.automation.selenium.core.LocalSeleniumGrid",
            "com.nordstrom.common.file.PathUtils", "org.apache.commons.lang3.StringUtils",
            "org.eclipse.jetty.util.Attributes", "javax.servlet.http.HttpServletResponse",
            "org.eclipse.jetty.http.HttpField", "org.openqa.selenium.chromium.ChromiumDriver",
            "org.openqa.selenium.grid.Main", "com.beust.jcommander.Strings", "org.openqa.selenium.remote.http.Route",
            "com.google.common.base.Utf8", "org.openqa.selenium.Keys", "org.openqa.selenium.remote.tracing.Tracer",
            "org.openqa.selenium.json.Json", "io.opentelemetry.sdk.autoconfigure.ResourceConfiguration",
            "io.opentelemetry.sdk.autoconfigure.spi.Ordered", "io.opentelemetry.api.trace.Span",
            "io.opentelemetry.sdk.trace.SdkSpan", "io.opentelemetry.context.Scope", "io.opentelemetry.sdk.metrics.View",
            "io.opentelemetry.sdk.logs.LogLimits", "io.opentelemetry.sdk.common.Clock",
            "io.opentelemetry.sdk.OpenTelemetrySdk", "org.zeromq.Utils", "dev.failsafe.Call", "graphql.Assert",
            "org.slf4j.MDC", "io.netty.channel.Channel", "io.netty.util.Timer", "io.netty.handler.ssl.SslUtils",
            "io.netty.buffer.ByteBuf", "io.netty.handler.codec.Headers", "io.netty.handler.codec.http.Cookie",
            "org.openqa.selenium.io.Zip", "ch.qos.logback.core.Layout", "io.netty.resolver.NameResolver",
            "io.opentelemetry.api.logs.Logger", "org.openqa.selenium.net.Urls", "org.dataloader.DataLoader",
            "com.google.common.util.concurrent.internal.InternalFutures", "org.eclipse.jetty.server.Server",
            "org.reactivestreams.Publisher", "org.openqa.selenium.manager.SeleniumManager",
            "org.apache.commons.exec.Executor", "io.netty.buffer.ByteBufUtil", 
            "io.netty.handler.codec.compression.ZlibCodecFactory", "io.opentelemetry.common.ComponentLoader",
            "com.github.benmanes.caffeine.cache.Weigher"};
    
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
        return 4;
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
    @SuppressWarnings("unchecked")
    public Path createHubConfig() throws IOException {
        Map<String, Object> hubConfig;
        // get path to hub configuration template
        String hubConfigPath = getHubConfigPath().toString();
        // create hub configuration from template
        try (Reader reader = Files.newBufferedReader(getHubConfigPath())) {
            hubConfig = new Json().toType(reader, MAP_TYPE);
        } catch (IOException e) {
            throw new ConfigException("Failed reading hub configuration template.", e);
        }
        
        String slotMatcher = getString(SeleniumSettings.SLOT_MATCHER.key());
        
        // strip extension to get template base path
        String configPathBase = hubConfigPath.substring(0, hubConfigPath.length() - 5);
        // get hash code of slot matcher as 8-digit hexadecimal string
        String hashCode = String.format("%08X", Objects.hash(slotMatcher));
        // assemble hub configuration file path with aggregated hash code
        Path filePath = Paths.get(configPathBase + "-" + hashCode + ".json");
        
        // if assembled path does not exist
        if (filePath.toFile().createNewFile()) {
            // add driver configuration
            Map<String, Object> distributorOptions = (Map<String, Object>) hubConfig.get("distributor");
            distributorOptions.put("slot-matcher", slotMatcher);
            try (OutputStream fos = new FileOutputStream(filePath.toFile());
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
    @SuppressWarnings("unchecked")
    public Path createNodeConfig(String capabilities, URL hubUrl) throws IOException {
        Map<String, Object> nodeConfig;
        boolean isAppium = capabilities.contains("appium");
        // get path to node configuration template
        String nodeConfigPath = getNodeConfigPath().toString();
        // create node configuration from template
        try (Reader reader = Files.newBufferedReader(getNodeConfigPath())) {
            nodeConfig = new Json().toType(reader, MAP_TYPE);
            Map<String, Object> nodeOptions = (Map<String, Object>) nodeConfig.computeIfAbsent("node", k -> new HashMap<>());
            nodeOptions.put("hub", hubUrl.getProtocol() + "://" + hubUrl.getAuthority() + GridServer.GRID_REGISTER);
            nodeOptions.computeIfAbsent("detect-drivers", k -> false);
            // if Appium
            if (isAppium) {
                // create relay configuration template if absent
                Map<String, Object> relayOptions = (Map<String, Object>) nodeConfig.computeIfAbsent("relay", k -> new HashMap<>());
                relayOptions.computeIfAbsent("host", k -> HostUtils.getLocalHost());
                relayOptions.computeIfAbsent("port", k -> PortProber.findFreePort());
                relayOptions.computeIfAbsent("configs", k -> new ArrayList<>());
            // otherwise (not Appium)
            } else {
                // add driver configuration template if absent
                nodeOptions.computeIfAbsent("driver-configuration", k -> new ArrayList<>());
            }
        } catch (IOException e) {
            throw new ConfigException("Failed reading node configuration template.", e);
        } catch (ClassCastException e) {
            throw new ConfigException("Failed unwrapping [node.driver-configuration] option", e);
        }
        
        // convert capabilities string to List<Map<String, Object>>
        String capsList = (capabilities.startsWith("[")) ? capabilities : "[" + capabilities + "]";
        List<Map<String, Object>> capsMapList = new Json().toType(capsList, LIST_OF_MAPS_TYPE);
        
        List<MutableCapabilities> capabilitiesList = capsMapList.stream()
                 .map(MutableCapabilities::new)
                 .map(theseCaps -> theseCaps.merge(getModifications(theseCaps, NODE_MODS_SUFFIX)))
                 .collect(Collectors.toList());
        
        // strip extension to get template base path
        String configPathBase = nodeConfigPath.substring(0, nodeConfigPath.length() - 5);
        // get hash code of capabilities list and hub URL as 8-digit hexadecimal string
        String hashCode = String.format("%08X", Objects.hash(capabilitiesList, hubUrl));
        // assemble node configuration file path with aggregated hash code
        Path filePath = Paths.get(configPathBase + "-" + hashCode + ".json");
        
        // if assembled path does not exist
        if (filePath.toFile().createNewFile()) {
            // if Appium
            if (isAppium) {
                // add relay slot specification 
                Map<String, Object> relayOptions = (Map<String, Object>) nodeConfig.get("relay");
                List<Object> configs = (List<Object>) relayOptions.get("configs");
                capabilitiesList.stream().forEach(theseCaps -> {
                    configs.add("1");
                    configs.add(toJson(theseCaps));
                });
            // otherwise (not Appium)
            } else {
                // add driver configuration
                Map<String, Object> nodeOptions = (Map<String, Object>) nodeConfig.get("node");
                List<Object> driverConfiguration = (List<Object>) nodeOptions.get("driver-configuration");
                capabilitiesList.stream().forEach(theseCaps -> {
                    Map<String, Object> thisConfig = new HashMap<>();
                    thisConfig.put("stereotype", theseCaps);
                    thisConfig.put("display-name", GridUtility.getPersonality(theseCaps));
                    Optional.ofNullable(GridUtility.getDriverPath(theseCaps))
                            .ifPresent(value -> thisConfig.put("webdriver-executable", value));
                    driverConfiguration.add(thisConfig);
                });
            }
            try (OutputStream fos = new FileOutputStream(filePath.toFile());
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
    public Capabilities[] getCapabilitiesForJson(String capabilities) {
        String capsList = (capabilities.startsWith("[")) ? capabilities : "[" + capabilities + "]";
        List<Map<String, Object>> capsMapList = new Json().toType(capsList, LIST_OF_MAPS_TYPE);
        return capsMapList.stream().map(MutableCapabilities::new).collect(Collectors.toList()).toArray(new Capabilities[0]);
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
