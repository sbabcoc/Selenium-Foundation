package com.nordstrom.automation.selenium.plugins;

import static org.openqa.selenium.json.Json.MAP_TYPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.grid.config.ConfigException;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.core.LocalSeleniumGrid.LocalGridServer;
import com.nordstrom.automation.selenium.core.GridServer;
import com.nordstrom.automation.selenium.exceptions.GridServerLaunchFailedException;
import com.nordstrom.automation.selenium.utility.BinaryFinder;
import com.nordstrom.common.file.PathUtils;

import net.bytebuddy.implementation.Implementation;

/**
 * This class provides the base plugin implementation for drivers provided by {@code appium}.
 * <p>
 * All of the Java driver classes associated with this plugin are contained in a single dependency:
 * 
 * <ul>
 *     <li><b>io.appium.java_client.android.AndroidDriver</b></li>
 *     <li><b>io.appium.java_client.android.IOSDriver</b></li>
 *     <li><b>io.appium.java_client.android.Mac2Driver</b></li>
 *     <li><b>io.appium.java_client.android.WindowsDriver</b></li>
 * </ul>
 * 
 * <pre>&lt;dependency&gt;
 *  &lt;groupId&gt;io.appium&lt;/groupId&gt;
 *  &lt;artifactId&gt;java-client&lt;/artifactId&gt;
 *  &lt;version&gt;7.6.0&lt;/version&gt;
 *  &lt;exclusions&gt;
 *    &lt;exclusion&gt;
 *      &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
 *      &lt;artifactId&gt;selenium-java&lt;/artifactId&gt;
 *    &lt;/exclusion&gt;
 *    &lt;exclusion&gt;
 *      &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
 *      &lt;artifactId&gt;selenium-support&lt;/artifactId&gt;
 *    &lt;/exclusion&gt;
 *  &lt;/exclusions&gt;
 *&lt;/dependency&gt;</pre>
 */
public abstract class AbstractAppiumPlugin implements DriverPlugin {

    private static final String[] DEPENDENCY_CONTEXTS = {};
    private static final String[] APPIUM_PATH_TAIL = { "appium", "build", "lib", "main.js" };
    private static final String[] PROPERTY_NAMES = 
        { SeleniumSettings.APPIUM_WITH_PM2.key(), SeleniumSettings.APPIUM_CLI_ARGS.key(), SeleniumSettings.NPM_BINARY_PATH.key(),
          SeleniumSettings.NODE_BINARY_PATH.key(), SeleniumSettings.PM2_BINARY_PATH.key(), SeleniumSettings.APPIUM_BINARY_PATH.key() };
    
    private static final Class<?>[] ARG_TYPES = {URL.class, Capabilities.class};
    
    private static final Pattern OPTION_PATTERN = Pattern.compile("\\s*(-[a-zA-Z0-9]+|--[a-zA-Z0-9]+(?:-[a-zA-Z0-9]+)*)");
    private static final String APPIUM_WITH_PM2 = "{\"nord:options\":{\"appiumWithPM2\":true}}";
    
    private final String browserName;
    
    protected AbstractAppiumPlugin(String browserName) {
        this.browserName = browserName;
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
    public String getBrowserName() {
        return browserName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPropertyNames(String capabilities) {
        return PROPERTY_NAMES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public LocalGridServer create(SeleniumConfig config, String launcherClassName, String[] dependencyContexts,
            URL hubUrl, Path workingPath, Path outputPath) throws IOException {
        
        String address;
        Integer portNum;
        List<String> argsList = new ArrayList<>();
        
        // create node configuration for this plug-in
        Path nodeConfigPath = config.createNodeConfig(getCapabilities(config), hubUrl);
        
        // allow specification of multiple command line arguments
        String[] cliArgs = config.getStringArray(SeleniumSettings.APPIUM_CLI_ARGS.key());
        // if args specified
        if (cliArgs != null) {
            int head = 0;
            int tail = 0;
            int next = 0;
            int index = 0;
            boolean doLoop;
            
            // iterate over specifications
            for (String thisArg : cliArgs) {
                doLoop = true;
                Matcher matcher = OPTION_PATTERN.matcher(thisArg);
                
                // until done
                while (doLoop) {
                    // save list end index
                    index = argsList.size();
                    
                    // if option found
                    if (matcher.find()) {
                        // add option to args list
                        argsList.add(matcher.group(1));
                        // set last value tail 
                        tail = matcher.start();
                        // save next value head
                        next = matcher.end() + 1;
                    // otherwise
                    } else {
                        // set final value tail
                        tail = thisArg.length();
                        // set 'done'
                        doLoop = false;
                    }
                    
                    // if maybe value
                    if (head < tail) {
                        // extract potential value, trimming ends
                        String value = thisArg.substring(head, tail).trim();
                        
                        // if value is defined
                        if ( ! value.isEmpty()) {
                            // insert at saved index
                            argsList.add(index, value);
                        }
                    }
                    
                    // advance
                    head = next;
                }
            }
        }
        
        // extract address and port from relay configuration
        try (Reader reader = Files.newBufferedReader(nodeConfigPath)) {
            Map<String, Object> nodeConfig = new Json().toType(reader, MAP_TYPE);
            Map<String, Object> relayOptions = (Map<String, Object>) nodeConfig.get("relay");
            address = (String) relayOptions.get("host");
            portNum = ((Long) relayOptions.get("port")).intValue();
        } catch (IOException e) {
            throw new ConfigException("Failed reading node configuration.", e);
        }
        
        // add driver specification
        argsList.add("--use-drivers");
        argsList.add(getBrowserName().toLowerCase());
        
        // specify server port
        argsList.add(0, portNum.toString());
        argsList.add(0, "--port");
        
        // specify server host
        argsList.add(0, address);
        argsList.add(0, "--address");
        
        ProcessBuilder builder;
        String appiumBinaryPath = findMainScript().getAbsolutePath();
        
        // if running with 'pm2'
        if (config.appiumWithPM2()) {
            File pm2Binary = findPM2Binary().getAbsoluteFile();

            argsList.add(0, "--");
            
            // if capturing output
            if (outputPath != null) {
                // specify 'pm2' log output path
                argsList.add(0, "\"" + outputPath.toString() + "\"");
                argsList.add(0, "--log");
            }
            
            // specify 'pm2' process name
            argsList.add(0, "appium-" + portNum);
            argsList.add(0, "--name");
            
            // specify path to 'appium' main script 
            argsList.add(0, "\"" + appiumBinaryPath + "\"");
            argsList.add(0, "start");
            
            String executable;
            if (SystemUtils.IS_OS_WINDOWS) {
                argsList.add(0, "\"" + pm2Binary.getAbsolutePath() + "\"");
                String command = String.join(" ", argsList);
                argsList.clear();
                
                executable = "cmd.exe";
                argsList.add("/c");
                argsList.add("\"" + command + "\"");
            } else {
                executable = pm2Binary.getAbsolutePath();
            }

            argsList.add(0, executable);
        // otherwise (running with 'node')
        } else {
            argsList.add(0, appiumBinaryPath);
            argsList.add(0, findNodeBinary().getAbsolutePath());
        }
        
        builder = new ProcessBuilder(argsList);
        
        // store path to relay configuration in Appium process environment
        builder.environment().put("nodeConfigPath", nodeConfigPath.toString());
        return new AppiumGridServer(address, portNum, false, builder, workingPath, outputPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Implementation getWebElementCtor(WebDriver driver, Class<? extends WebElement> refClass) {
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends RemoteWebDriver> Constructor<T> getRemoteWebDriverCtor(Capabilities desiredCapabilities) {
        String automationName = (String) desiredCapabilities.getCapability("appium:automationName");
        if (automationName == null) {
            automationName = (String) desiredCapabilities.getCapability("automationName");
        }
        if (getBrowserName().equalsIgnoreCase(automationName)) {
            try {
                return (Constructor<T>) Class.forName(getDriverClassName()).getConstructor(ARG_TYPES);
            } catch (SecurityException | ClassNotFoundException | NoSuchMethodException | ClassCastException eaten) {
                // nothing to do here
            }
        }
        return null;
    }

    /**
     * Get the name of the {@link WebDriver} implementation for this plug-in.
     * 
     * @return driver-specific {@link WebDriver} class name
     */
    public abstract String getDriverClassName();
    
    /**
     * Add the 'nord:options' object to the specified node capabilities string. <br>
     * <b>NOTE</b>: The 'nord:options' object is only added if Appium is being managed by PM2.
     * 
     * @param config {@link SelenikumConfig} object
     * @param nodeCapabilities node capabilities string
     * @return node capabilities string 
     */
    String addNordOptions(SeleniumConfig config, String nodeCapabilities) {
        // if not running with 'pm2', no options to add
        if (!config.appiumWithPM2()) return nodeCapabilities;
        
        // add indication of stand-alone execution of 'appium' with 'pm2'
        Capabilities capabilities = config.getCapabilitiesForJson(nodeCapabilities)[0];
        Capabilities nordOptions = config.getCapabilitiesForJson(APPIUM_WITH_PM2)[0];
        return config.toJson(config.mergeCapabilities(capabilities, nordOptions));
    }
    
    /**
     * Find the 'npm' (Node Package Manager) binary.
     * 
     * @return path to the 'npm' binary as a {@link File} object
     * @throws GridServerLaunchFailedException if 'npm' isn't found
     */
    private static File findNPMBinary() throws GridServerLaunchFailedException {
        return findBinary("npm", SeleniumSettings.NPM_BINARY_PATH, "'npm' package manager");
    }
    
    /**
     * Find the 'node' binary.
     * 
     * @return path to the 'node' binary as a {@link File} object
     * @throws GridServerLaunchFailedException if 'node' isn't found
     */
    private static File findNodeBinary() throws GridServerLaunchFailedException {
        return findBinary("node", SeleniumSettings.NODE_BINARY_PATH, "'node' JavaScript runtime");
    }
    
    /**
     * Find the 'pm2' binary.
     * 
     * @return path to the 'pm2' binary as a {@link File} object
     * @throws GridServerLaunchFailedException if 'node' isn't found
     */
    private static File findPM2Binary() throws GridServerLaunchFailedException {
        return findBinary("pm2", SeleniumSettings.PM2_BINARY_PATH, "'pm2' process manager");
    }
    
    /**
     * Find the 'appium' main script in the global 'node' modules repository.
     * 
     * @return path path to the 'appium' main script as a {@link File} object
     * @throws GridServerLaunchFailedException if the 'appium' main script isn't found
     */
    private static File findMainScript() throws GridServerLaunchFailedException {
        // check configuration for path to 'appium' main script
        try {
            return findBinary("main.js", SeleniumSettings.APPIUM_BINARY_PATH, "'appium' main script");
        } catch (GridServerLaunchFailedException eaten) {
            // path not specified - check modules repository below
        }
        
        // check for 'appium' main script in global 'node' modules repository
        
        String nodeModulesRoot;
        File npm = findNPMBinary().getAbsoluteFile();
        
        String executable;
        List<String> argsList = new ArrayList<>();
        
        if (SystemUtils.IS_OS_WINDOWS) {
            executable = "cmd.exe";
            argsList.add("/c");
            argsList.add("\"" + npm.getAbsolutePath() + "\"");
        } else {
            executable = npm.getAbsolutePath();
        }
        
        argsList.add("root");
        argsList.add("-g");
        
        argsList.add(0, executable);
        ProcessBuilder builder = new ProcessBuilder(argsList);
        builder.environment().put("PATH", PathUtils.getSystemPath());
        
        try {
            Process process = builder.start();
            process.waitFor();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                nodeModulesRoot = reader.lines().parallel().collect(Collectors.joining("\n"));
            }
            
            int index = nodeModulesRoot.lastIndexOf('\n');
            if (index > 0) nodeModulesRoot = nodeModulesRoot.substring(index).trim();
            File appiumMain = Paths.get(nodeModulesRoot, APPIUM_PATH_TAIL).toFile();
            if (appiumMain.exists()) return appiumMain;
            throw fileNotFound("'appium' main script", SeleniumSettings.APPIUM_BINARY_PATH);
        } catch (IOException cause) {
            throw new GridServerLaunchFailedException("node", cause);
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
            throw new GridServerLaunchFailedException("node", cause);
        }
    }
    
    /**
     * Find the specified binary.
     * 
     * @param exeName file name of binary to find
     * @param setting associated configuration setting
     * @param what human-readable description of binary
     * @return path to specified binary as a {link File} object
     * @throws GridServerLaunchFailedException if specified binary isn't found
     */
    private static File findBinary(String exeName, SeleniumSettings setting, String what)
            throws GridServerLaunchFailedException {
        try {
            return BinaryFinder.findBinary(exeName, setting.key());
        } catch (IllegalStateException eaten) {
            IOException cause = fileNotFound(what, setting);
            throw new GridServerLaunchFailedException("node", cause);
        }
    }
    
    /**
     * Assemble a 'file not found' exception for the indicated binary.
     * 
     * @param what human-readable description of binary
     * @param setting associated configuration setting
     * @return {@link FileNotFoundException} object
     */
    private static IOException fileNotFound(String what, SeleniumSettings setting) {
        String template = "%s not found; configure the %s setting (key: %s)";
        return new FileNotFoundException(String.format(template, what, setting.name(), setting.key()));
    }

    public static class AppiumGridServer extends LocalGridServer {

        public AppiumGridServer(String host, Integer port, boolean isHub, ProcessBuilder builder, Path workingPath, Path outputPath) {
            super(host, port, isHub, builder, workingPath, outputPath);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean shutdown(final boolean localOnly) throws InterruptedException {
            if (isActive()) {
                if ( ! shutdownAppiumWithPM2(getUrl())) {
                    getProcess().destroy();
                }
            }
            return true;
        }
        
        /**
         * Get stored relay node configuration path. <br>
         * <b>NOTE</b>: A relay node is needed to connect the Appium server to a Selenium 4+ Grid hub.
         * 
         * @return path to relay node configuration; may be {@code null}
         */
        public Path getNodeConfigPath() {
            String nodeConfigPath = getEnvironment().get("nodeConfigPath");
            return (nodeConfigPath != null) ? Paths.get(nodeConfigPath) : null;
        }
        
        /**
         * If the specified URL is a local 'appium' node running with 'pm2', delete the process.
         * 
         * @param nodeUrl {@link URL} object for target node server
         * @return {@code true} node was shut down'; otherwise {@code false}
         */
        public static boolean shutdownAppiumWithPM2(URL nodeUrl) {
            if ( ! GridUtility.isLocalHost(nodeUrl)) return false;
            if ( ! isAppiumWithPM2(nodeUrl)) return false;
            
            int exitCode = 0;
            String executable;
            ProcessBuilder builder;
            List<String> argsList = new ArrayList<>();
            File pm2Binary = findPM2Binary().getAbsoluteFile();

            argsList.add("delete");
            argsList.add("appium-" + nodeUrl.getPort());
            
            if (SystemUtils.IS_OS_WINDOWS) {
                executable = "cmd.exe";
                argsList.add(0, "\"" + pm2Binary.getAbsolutePath() + "\"");
                argsList.add(0, "/c");
            } else {
                executable = pm2Binary.getAbsolutePath();
            }
            
            argsList.add(0, executable);
            builder = new ProcessBuilder(argsList);
            builder.environment().put("PATH", PathUtils.getSystemPath());
            
            try {
                exitCode = builder.start().waitFor();
            } catch (IOException eaten) {
                exitCode = -1;
            } catch (InterruptedException cause) {
                Thread.currentThread().interrupt();
                exitCode = -1;
            }
            
            return exitCode == 0;
        }

        /**
         * Determine if using 'pm2' for stand-alone execution of 'appium'.
         * 
         * @param nodeUrl {@link URL} object for target node server
         * @return {@code true} if using 'pm2'; otherwise {@code false}
         */
        public static boolean isAppiumWithPM2(URL nodeUrl) {
            SeleniumConfig config = SeleniumConfig.getConfig();
            // check settings to determine if 'pm2' used for stand-alone execution of 'appium'
            boolean appiumWithPM2 = config.getBoolean(SeleniumSettings.APPIUM_WITH_PM2.key());
            // if undetermined
            if (!appiumWithPM2) {
                try {
                    // get capabilities of 'appium' node
                    Capabilities capabilities = 
                            GridServer.getNodeCapabilities(config, config.getHubUrl(), nodeUrl).get(0);
                    // get map of custom options from capabilities
                    Map<String, Object> options = GridUtility.getNordOptions(capabilities);
                    // determine is running 'appium' with 'pm2'
                    appiumWithPM2 = options.containsKey("appiumWithPM2");
                } catch (IndexOutOfBoundsException | IOException eaten) {
                    // nothing to do here
                }
            }
            return appiumWithPM2;
        }
    }
}
