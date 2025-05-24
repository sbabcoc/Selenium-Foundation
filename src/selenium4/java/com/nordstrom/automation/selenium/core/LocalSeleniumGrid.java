package com.nordstrom.automation.selenium.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.concurrent.TimeoutException;
import org.openqa.selenium.net.PortProber;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.exceptions.GridServerLaunchFailedException;
import com.nordstrom.automation.selenium.plugins.AbstractAppiumPlugin.AppiumGridServer;
import com.nordstrom.automation.selenium.utility.HostUtils;
import com.nordstrom.common.base.UncheckedThrow;
import com.nordstrom.common.file.PathUtils;
import com.nordstrom.common.jar.JarUtils;
import com.nordstrom.common.uri.UriUtils;

/**
 * This class launches Selenium Grid server instances, each in its own system process. Clients of this class specify
 * the role of the server (either {@code hub} or {@code node}), and they get a {@link Process} object for managing
 * the server lifetime as a result.
 * <p>
 * The output of the process is redirected to a file named <u>grid-<i>&lt;role&gt;</i>.log</u> in the test context
 * output directory. Process error output is redirected, so this log file will contain both standard output and errors.
 * <p>
 * <b>NOTE</b>: If no test context is specified, the log file will be stored in the "current" directory of the parent
 * Java process.  
 */
public class LocalSeleniumGrid extends SeleniumGrid {

    private static final String OPT_HOST = "--host";
    private static final String OPT_PORT = "--port";
    private static final String OPT_CONFIG = "--config";
    
    /**
     * Constructor for models of local Selenium Grid instances from hub URL.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubServer {@link LocalGridServer} instance for a local grid hub process
     * @param nodeServers {@link LocalGridServer} instances for local grid node processes (zero or more)
     * @throws IOException if unable to acquire local Grid details
     */
    public LocalSeleniumGrid(SeleniumConfig config, LocalGridServer hubServer, LocalGridServer... nodeServers) throws IOException {
        super(config, hubServer, nodeServers);
    }
    
    /**
     * Activate this <b>LocalSeleniumGrid</b> instance.
     * <p>
     * This method ensures that the hub and node servers associated with this local grid are launched and active,
     * and it also ensures that grid node servers are registered with the hub. 
     * 
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if this thread was interrupted
     * @throws TimeoutException if host timeout interval exceeded
     */
    public void activate() throws IOException, InterruptedException, TimeoutException {
        GridServer hubServer = getHubServer();
        Collection<GridServer> nodeServers = getNodeServers().values();
        
        ((LocalGridServer) hubServer).start();
        for (GridServer nodeServer : nodeServers) {
            ((LocalGridServer) nodeServer).start();
        }
        
        awaitGridReady(hubServer, nodeServers);
    }

    /**
     * Wait until the indicated Grid collection is entirely ready.
     * 
     * @param hubServer Grid hub server
     * @param nodeServers collection of Grid node servers
     * @throws InterruptedException if this thread was interrupted
     * @throws TimeoutException if host timeout interval exceeded
     */
    public static void awaitGridReady(GridServer hubServer, Collection<GridServer> nodeServers)
            throws TimeoutException, InterruptedException {
        
        SeleniumConfig config = SeleniumConfig.getConfig();
        long maxWait = config.getLong(SeleniumSettings.HOST_TIMEOUT.key()) * 1000;
        long maxTime = System.currentTimeMillis() + maxWait;
        while (!isGridReady(config, hubServer, nodeServers)) {
            if ((maxWait > 0) && (System.currentTimeMillis() > maxTime)) {
                throw new TimeoutException("Timed out waiting for Grid collection to be ready");
            }
            Thread.sleep(100);
        }
    }
    
    /**
     * Determine if the indicated Grid collection is entirely ready.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubServer Grid hub server
     * @param nodeServers collection of Grid node servers
     * @return {@code true} if the entire Grid collection is ready; otherwise {@code false}
     */
    static boolean isGridReady(SeleniumConfig config, GridServer hubServer, Collection<GridServer> nodeServers) {
        if (!GridServer.isHubActive(hubServer.getUrl())) return false;
        for (GridServer nodeServer : nodeServers) {
            // if not an Appium Grid server
            if (!(nodeServer instanceof AppiumGridServer)) {
                if (!GridServer.isNodeRegistered(config, hubServer.getUrl(), nodeServer.getUrl())) return false;
            }
        }
        return true;
    }
    
    /**
     * Create an object that represents the local Selenium Grid instance.
     * <p>
     * <b>NOTE</b>: This method does <b>NOT</b> activate the represented local Grid instance.
     * <b>NOTE</b>: This method stores the hub host URL in the {@link SeleniumSettings#HUB_HOST HUB_HOST} property for
     * subsequent retrieval.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubUrl {@link URL} for grid hub host (may be {@code null})
     * @return {@link SeleniumGrid} object for local Grid
     * @throws IOException if an I/O error occurs
     * @throws ServiceConfigurationError if unable to instantiate all configured driver plugins
     * @see #activate()
     */
    public static SeleniumGrid create(SeleniumConfig config, final URL hubUrl) throws IOException {
        GridServer hubServer = null;
        SeleniumGrid seleniumGrid = null;
        
        Objects.requireNonNull(config, "[config] must be non-null");
        
        String launcherClassName = config.getString(SeleniumSettings.GRID_LAUNCHER.key());
        String[] dependencyContexts = config.getDependencyContexts();
        String workingDir = config.getString(SeleniumSettings.GRID_WORKING_DIR.key());
        Path workingPath = (workingDir == null || workingDir.isEmpty()) ? null : Paths.get(workingDir);
        
        // if specified hub is already active
        if (hubUrl != null && GridServer.isHubActive(hubUrl)) {
            seleniumGrid =  new SeleniumGrid(config, hubUrl);
            hubServer = seleniumGrid.getHubServer();
        // otherwise (creating new local Grid)
        } else {
            // create hub configuration
            Path hubConfigPath = config.createHubConfig();
            // get hub port:
            // - if hub URL if specified, extract port from URL
            // - otherwise, use specified hub port, defaulting to -1
            Integer hubPort = (hubUrl != null) ?
                    hubUrl.getPort() : config.getInteger(SeleniumSettings.HUB_PORT.key(), -1);
            Path outputPath = GridUtility.getOutputPath(config, true);
            hubServer = create(config, launcherClassName, dependencyContexts, 
                    true, hubPort, hubConfigPath, workingPath, outputPath);
        }
        
        // store hub host and hub port in system properties for subsequent retrieval
        System.setProperty(SeleniumSettings.HUB_HOST.key(), hubServer.getUrl().toString());
        System.setProperty(SeleniumSettings.HUB_PORT.key(), Integer.toString(hubServer.getUrl().getPort()));
        
        List<GridServer> nodeServers = new ArrayList<>();
        
        // iterate over configured driver plugins
        for (DriverPlugin driverPlugin : GridUtility.getDriverPlugins(config)) {
            // if creating new local Grid or active Grid doesn't include current driver plug-in
            if (seleniumGrid == null || !seleniumGrid.personalities.containsKey(driverPlugin.getBrowserName())) {
                // create node server for this driver plug-in
                LocalGridServer nodeServer = driverPlugin.create(config, launcherClassName, dependencyContexts,
                        hubServer.getUrl(), workingPath);
                // add server to nodes list
                nodeServers.add(nodeServer);
                // if this is an Appium Grid server
                if (nodeServer instanceof AppiumGridServer) {
                    // get path to relay configuration path from Appium process environment
                    Path nodeConfigPath = ((AppiumGridServer) nodeServer).getNodeConfigPath();
                    // add relay node for Appium Grid server to nodes list
                    nodeServers.add(create(config, launcherClassName, dependencyContexts, false, -1, nodeConfigPath,
                            workingPath, GridUtility.getOutputPath(config, null)));
                    LOGGER.debug("Adding local Grid relay for Appium server providing personalities: {}",
                            nodeServer.getPersonalities().keySet());
                } else {
                    LOGGER.debug("Adding local Grid node providing personalities: {}",
                            nodeServer.getPersonalities().keySet());
                }
            }
        }
        
        // if graphing active Grid
        if (seleniumGrid != null) {
            // if no local nodes added
            if (nodeServers.isEmpty()) {
                return seleniumGrid;
            // otherwise (local nodes added)
            } else {
                try {
                    for (GridServer nodeServer : nodeServers) {
                        ((LocalGridServer) nodeServer).start();
                    }
                    awaitGridReady(hubServer, nodeServers);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted activating new local nodes", e);
                } catch (IOException | TimeoutException e) {
                    throw new IllegalStateException("Failed activating new local nodes", e);
                }
            }
            
            return new SeleniumGrid(config, hubUrl);
        } else {
            return new LocalSeleniumGrid(config, (LocalGridServer) hubServer, nodeServers.toArray(new LocalGridServer[0]));
        }
    }

    /**
     * Create an object that represents a Selenium Grid server with the specified arguments.
     * <p>
     * <b>NOTE</b>: The created object defines a separate process for managing the local server, but does <b>NOT</b>
     * start this process.
     * 
     * @param config {@link SeleniumConfig} object
     * @param launcherClassName fully-qualified name of {@code GridLauncher} class
     * @param dependencyContexts fully-qualified names of context classes for Selenium Grid dependencies
     * @param isHub role of Grid server being started ({@code true} = hub; {@code false} = node)
     * @param port port that Grid server should use; -1 to specify auto-configuration
     * @param configPath {@link Path} to server configuration file
     * @param workingPath {@link Path} of working directory for server process; {@code null} for default
     * @param outputPath {@link Path} to output log file; {@code null} to decline log-to-file
     * @param propertyNames optional array of property names to propagate to server process
     * @return {@link LocalGridServer} object for managing the server process
     * @throws GridServerLaunchFailedException If a Grid component process failed to start
     * @see #activate()
     * @see LocalGridServer#start()
     * @see <a href="http://www.seleniumhq.org/docs/07_selenium_grid.jsp#getting-command-line-help">
     *      Getting Command-Line Help</a>
     */
    public static LocalGridServer create(final SeleniumConfig config, final String launcherClassName,
            final String[] dependencyContexts, final boolean isHub, final Integer port, final Path configPath,
            final Path workingPath, final Path outputPath, final String... propertyNames) {
        
        List<String> argsList = new ArrayList<>();
        
        String gridRole = isHub ? "hub" : "node";
        boolean doDebug = isHub ? 
                config.getBoolean(SeleniumSettings.HUB_DEBUG.key()) :
                config.getBoolean(SeleniumSettings.NODE_DEBUG.key());
        String address = doDebug ? (isHub ? "8000" : "8001") : null;
        
        // specify server role
        argsList.add(gridRole);
        
        String hostUrl = HostUtils.getLocalHost();
        
        // specify server host
        argsList.add(OPT_HOST);
        argsList.add(hostUrl);
        
        Integer portNum = port;
        // if port auto-select spec'd
        if (portNum == -1) {
            // acquire available port
            portNum = PortProber.findFreePort();
        }
        
        // specify server port
        argsList.add(OPT_PORT);
        argsList.add(portNum.toString());
        
        // specify server configuration file
        argsList.add(OPT_CONFIG);
        argsList.add(configPath.toString());
        
        // specify Grid launcher class name
        argsList.add(0, launcherClassName);
        
        // propagate Java System properties
        for (String name : propertyNames) {
            String value = System.getProperty(name);
            if (value != null) {
                argsList.add(0, "-D" + name + "=" + value);
            }
        }
        
        // get dependency context paths
        List<String> contextPaths = JarUtils.getContextPaths(dependencyContexts);
        // extract classpath specification
        String classPath = contextPaths.remove(0);
        // for each specified Java agent...
        for (String agentSpec : contextPaths) {
            // ... specify a 'javaagent' argument
            argsList.add(0, agentSpec);
        }
        
        // specify Java class path
        argsList.add(0, classPath);
        argsList.add(0, "-cp");
        
        // if debugging spec'd
        if (address != null) {
            // add JDWP library: suspend grid server on launch, listen on 8000 (hub) or 8001 (node)
            argsList.add(0, "-agentlib:jdwp=transport=dt_socket,server=y,address=" + address);
        }
        
        argsList.add(0, System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        ProcessBuilder builder = new ProcessBuilder(argsList);
        builder.environment().put("PATH", PathUtils.getSystemPath());
        return new LocalGridServer(hostUrl, portNum, isHub, builder, workingPath, outputPath);
    }

    /**
     * This class represents a single Selenium server (hub or node) belonging to a local Grid collection.
     */
    public static class LocalGridServer extends GridServer {

        private final ProcessBuilder builder;
        private Process process = null;
        private boolean hasStarted = false;
        private boolean isActive = false;
        private final Map<String, String> personalities = new HashMap<>();
        
        /**
         * Constructor for local Grid server object.
         * 
         * @param host IP address of local Grid server
         * @param port port of local Grid server
         * @param isHub role of Grid server being started ({@code true} = hub; {@code false} = node)
         * @param builder {@link ProcessBuilder} of local Grid server
         * @param workingPath {@link Path} of working directory for server process; {@code null} for default
         * @param outputPath {@link Path} to output log file; {@code null} to decline log-to-file
         */
        public LocalGridServer(String host, Integer port, boolean isHub, ProcessBuilder builder, Path workingPath, Path outputPath) {
            super(getServerUrl(host, port), isHub);
            
            if (workingPath != null) {
                builder.directory(workingPath.toFile());
            }
            
            if (outputPath != null) {
                builder.redirectOutput(outputPath.toFile());
            }
            
            this.builder = builder;
        }
        
        /**
         * Get process environment of this local grid server.
         *  
         * @return map of process environment variables
         */
        public Map<String, String> getEnvironment() {
            return builder.environment();
        }
        
        /**
         * Get the driver 'personalities' for this local Grid server.
         * 
         * @return map: "personality" &rarr; desired capabilities (JSON)
         */
        public Map<String, String> getPersonalities() {
            return personalities;
        }
        
        /**
         * Start the process associated with this local Grid server object.
         * <p>
         * This method ensures that this local Grid server (hub or node) is launched and active, and it also ensures
         * that Grid node servers are registered with their configured hub. 
         * 
         * @throws IOException if an I/O error occurs
         * @throws InterruptedException if this thread was interrupted
         * @throws TimeoutException if host timeout interval exceeded
         */
        public void start() throws IOException, InterruptedException, TimeoutException {
            if (!hasStarted) {
                process = builder.start();
                hasStarted = true;
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isActive() {
            if (!isActive) {
                isActive = super.isActive();
            }
            return isActive;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean shutdown() throws InterruptedException {
            if (!isActive()) return true;
            
            if (ServerProcessKiller.killServerProcess(process, getUrl())) {
                hasStarted = false;
                isActive = false;
                return true;
            }
            
            return false;
        }
        
        /**
         * Get {@code localhost} URL for Selenium Grid server at the specified port.
         * <p>
         * <b>NOTE</b>: The assembled URL includes the Grid web service base path.
         * 
         * @param host IP address of local Grid server
         * @param port port of local Grid server
         * @return {@link URL} for local Grid server at the specified port
         */
        public static URL getServerUrl(String host, Integer port) {
            try {
                return UriUtils.makeBasicURI("http", host, port, GridServer.HUB_BASE).toURL();
            } catch (MalformedURLException e) {
                throw UncheckedThrow.throwUnchecked(e);
            }
        }
    }
}
