package com.nordstrom.automation.selenium;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.openqa.grid.common.GridRole;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.gson.Gson;
import com.nordstrom.automation.settings.SettingsCore;

/**
 * This class declares settings and methods related to WebDriver and Grid configuration specific to the Selenium 2 API.
 * 
 * @see SettingsCore
 */
@SuppressWarnings({"squid:S1200", "squid:S2972", "squid:MaximumInheritanceDepth"})
public class SeleniumConfig extends AbstractSeleniumConfig {
    
    private static final String HUB = "hub";
    
    private static final String JSON_HEAD = "{ \"capabilities\": [";
    private static final String JSON_TAIL = "], \"configuration\": {} }";
    private static final String GRID_LAUNCHER = "org.openqa.grid.selenium.GridLauncher";
    private static final String HUB_PORT = "4444";
    private static final String NODE_CONFIG = "nodeConfig-s2.json";
    
    /**
     * <b>com.google.common.util.concurrent.SimpleTimeLimiter</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.google.guava&lt;/groupId&gt;
     *  &lt;artifactId&gt;guava&lt;/artifactId&gt;
     *  &lt;version&gt;21.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.http.conn.HttpClientConnectionManager</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.apache.httpcomponents&lt;/groupId&gt;
     *  &lt;artifactId&gt;httpclient&lt;/artifactId&gt;
     *  &lt;version&gt;4.5.1&lt;/version&gt;
     *&lt;/dependency&gt;
     * 
     * <b>org.apache.http.config.RegistryBuilder</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.apache.httpcomponents&lt;/groupId&gt;
     *  &lt;artifactId&gt;httpcore&lt;/artifactId&gt;
     *  &lt;version&gt;4.4.3&lt;/version&gt;
     *&lt;/dependency&gt;
     * 
     * <b>org.apache.commons.logging.LogFactory</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;commons-logging&lt;/groupId&gt;
     *  &lt;artifactId&gt;commons-logging&lt;/artifactId&gt;
     *  &lt;version&gt;1.2&lt;/version&gt;
     * 
     * <b>org.openqa.selenium.support.events.WebDriverEventListener</b>
     * 
     *&lt;/dependency&gt;
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-support&lt;/artifactId&gt;
     *  &lt;version&gt;2.53.1&lt;/version&gt;
     *&lt;/dependency&gt;
     * 
     * <b>org.openqa.selenium.WebDriverException</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-api&lt;/artifactId&gt;
     *  &lt;version&gt;2.53.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
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
     * <b>com.beust.jcommander.JCommander</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.beust&lt;/groupId&gt;
     *  &lt;artifactId&gt;jcommander&lt;/artifactId&gt;
     *  &lt;version&gt;1.48&lt;/version&gt;
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
     * <b>mx4j.remote.HeartBeat</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;mx4j&lt;/groupId&gt;
     *  &lt;artifactId&gt;mx4j-tools&lt;/artifactId&gt;
     *  &lt;version&gt;3.0.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>net.jcip.annotations.ThreadSafe</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;net.jcip&lt;/groupId&gt;
     *  &lt;artifactId&gt;jcip-annotations&lt;/artifactId&gt;
     *  &lt;version&gt;1.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.bouncycastle.crypto.BlockCipher</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.bouncycastle&lt;/groupId&gt;
     *  &lt;artifactId&gt;bcprov-jdk15on&lt;/artifactId&gt;
     *  &lt;version&gt;1.48&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.bouncycastle.openssl.PEMKeyPair</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.bouncycastle&lt;/groupId&gt;
     *  &lt;artifactId&gt;bcpkix-jdk15on&lt;/artifactId&gt;
     *  &lt;version&gt;1.48&lt;/version&gt;
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
     * <b>org.openqa.jetty.util.MultiException</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;jetty-rc-repacked&lt;/artifactId&gt;
     *  &lt;version&gt;5&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.yaml.snakeyaml.Yaml</b> (for selenium-server)
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.yaml&lt;/groupId&gt;
     *  &lt;artifactId&gt;snakeyaml&lt;/artifactId&gt;
     *  &lt;version&gt;1.8&lt;/version&gt;
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
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
                    "com.google.common.util.concurrent.SimpleTimeLimiter",
                    "org.apache.http.conn.HttpClientConnectionManager",
                    "org.apache.http.config.RegistryBuilder",
                    "org.apache.commons.logging.LogFactory",
                    "org.openqa.selenium.support.events.WebDriverEventListener",
                    "org.openqa.selenium.WebDriverException",
                    GRID_LAUNCHER, "com.beust.jcommander.JCommander",
                    "javax.servlet.http.HttpServlet", "mx4j.remote.HeartBeat",
                    "net.jcip.annotations.ThreadSafe", "org.bouncycastle.crypto.BlockCipher",
                    "org.bouncycastle.openssl.PEMKeyPair", "org.seleniumhq.jetty9.util.Jetty",
                    "org.openqa.jetty.util.MultiException", "org.yaml.snakeyaml.Yaml",
                    "com.google.gson.JsonIOException",
                    "org.openqa.selenium.remote.JsonToBeanConverter"};
    
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
            String input = JSON_HEAD + capabilities + JSON_TAIL;
            List<DesiredCapabilities> capabilitiesList = RegistrationRequest.getNewInstance(input).getCapabilities();
            RegistrationRequest nodeConfig = new RegistrationRequest();
            nodeConfig.loadFromJSON(nodeConfigPath);
            nodeConfig.setCapabilities(capabilitiesList);
            nodeConfig.getConfiguration().put(HUB, hubUrl.toString());

            // hack for RegistrationRequest bug
            nodeConfig.setRole(GridRole.NODE);
            
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(filePath.toFile()))) {
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
        return RegistrationRequest.getNewInstance(input).getCapabilities().stream().toArray(Capabilities[]::new);
    }
    
    /**
     * {@inheritDoc}
     */
    public String toJson(Capabilities capabilities) {
        return new Gson().toJsonTree(capabilities.asMap()).toString();
    }
}
