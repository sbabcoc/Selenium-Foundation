package com.nordstrom.automation.selenium.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.TimeoutException;
import org.openqa.grid.common.GridRole;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.os.CommandLine;

import com.google.common.collect.ImmutableList;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.exceptions.GridServerLaunchFailedException;
import com.nordstrom.common.base.UncheckedThrow;
import com.nordstrom.common.jar.JarUtils;

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

    private static final String OPT_ROLE = "-role";
    private static final String OPT_HOST = "-host";
    private static final String OPT_PORT = "-port";
    
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
        ((LocalGridServer) getHubServer()).start();
        for (GridServer server : getNodeServers().values()) {
            ((LocalGridServer) server).start();
        }
    }
    
    /**
     * Create an object that represents the local Selenium Grid instance.
     * <p>
     * <b>NOTE</b>: This method does <b>NOT</b> activate the represented local Grid instance.
     * <b>NOTE</b>: This method stores the hub host URL in the {@link SeleniumSettings#HUB_HOST HUB_HOST} property for
     * subsequent retrieval.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubConfigPath Selenium Grid hub configuration path
     * @return {@link SeleniumGrid} object for local Grid
     * @throws IOException if an I/O error occurs
     * @throws ServiceConfigurationError if unable to instantiate all configured driver plugins
     * @see #activate()
     */
    public static SeleniumGrid create(SeleniumConfig config, final Path hubConfigPath) throws IOException {
        
        String launcherClassName = config.getString(SeleniumSettings.GRID_LAUNCHER.key());
        String[] dependencyContexts = config.getDependencyContexts();
        Integer hubPort = config.getInteger(SeleniumSettings.HUB_PORT.key(), 0);
        String workingDir = config.getString(SeleniumSettings.GRID_WORKING_DIR.key());
        Path workingPath = (workingDir == null || workingDir.isEmpty()) ? null : Paths.get(workingDir);
        Path outputPath = GridUtility.getOutputPath(config, GridRole.HUB);
        LocalGridServer hubServer = create(launcherClassName, dependencyContexts, GridRole.HUB,
                        hubPort, hubConfigPath, workingPath, outputPath);
        
        // store hub host and hub port in system properties for subsequent retrieval
        System.setProperty(SeleniumSettings.HUB_HOST.key(), hubServer.getUrl().toString());
        System.setProperty(SeleniumSettings.HUB_PORT.key(), Integer.toString(hubServer.getUrl().getPort()));
        
        List<LocalGridServer> nodeServers = new ArrayList<>();
        for (DriverPlugin driverPlugin : getDriverPlugins(config)) {
            outputPath = GridUtility.getOutputPath(config, GridRole.NODE);
            LocalGridServer nodeServer = driverPlugin.create(config, launcherClassName, dependencyContexts, hubServer,
                    workingPath, outputPath);
            nodeServer.personalities.putAll(driverPlugin.getPersonalities());
            nodeServers.add(nodeServer);
        }
        
        return new LocalSeleniumGrid(config, hubServer, nodeServers.toArray(new LocalGridServer[0]));
    }

    /**
     * Get instances of all configured driver plugins.
     * 
     * @param config {@link SeleniumConfig} object
     * @return list of driver plugin instances
     */
    static List<DriverPlugin> getDriverPlugins(SeleniumConfig config) {
        List<DriverPlugin> driverPlugins;
        
        // get grid plugins setting
        String gridPlugins = config.getString(SeleniumSettings.GRID_PLUGINS.key());
        // if setting is defined
        if (gridPlugins != null) {
            driverPlugins = new ArrayList<>();
            // iterate specified driver plugin class names
            for (String driverPlugin : gridPlugins.split(File.pathSeparator)) {
                String className = driverPlugin.trim();
                try {
                    // load driver plugin class
                    Class<?> pluginClass = Class.forName(className);
                    // get no-argument constructor
                    Constructor<?> ctor = pluginClass.getConstructor();
                    // add instance to plugins list
                    driverPlugins.add((DriverPlugin) ctor.newInstance());
                } catch (ClassNotFoundException e) {
                    throw new ServiceConfigurationError("Specified driver plugin '" + className + "' not found", e);
                } catch (ClassCastException e) {
                    throw new ServiceConfigurationError("Specified driver plugin '" + className
                            + "' is not a subclass of DriverPlugin", e);
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new ServiceConfigurationError("Specified driver plugin '" + className
                            + "' lacks an accessible no-argument constructor", e);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
                    throw new ServiceConfigurationError("Specified driver plugin '" + className
                            + "' could not be instantiated", e);
                } catch (InvocationTargetException e) {
                    throw new ServiceConfigurationError("Constructor for driver plugin '" + className
                            + "' threw an exception", e.getTargetException());
                }
            }
        } else {
            // get service loader for driver plugins
            ServiceLoader<DriverPlugin> serviceLoader = ServiceLoader.load(DriverPlugin.class);
            // collect list of configured plugins
            driverPlugins = ImmutableList.copyOf(serviceLoader.iterator());
        }
        
        return driverPlugins;
    }

    /**
     * Wait for the specified Grid server to indicate that it's ready.
     * 
     * @param server {@link LocalGridServer} object to wait for.
     * @param maxWait maximum interval in milliseconds to wait; negative interval to wait indefinitely
     * @throws InterruptedException if this thread was interrupted
     * @throws TimeoutException if not waiting indefinitely and exceeded maximum wait
     */
    protected static void waitUntilReady(LocalGridServer server, long maxWait)
                    throws InterruptedException, TimeoutException {
        long maxTime = System.currentTimeMillis() + maxWait;
        while (!server.isActive()) {
            if ((maxWait > 0) && (System.currentTimeMillis() > maxTime)) {
                throw new TimeoutException("Timed out waiting for Grid server to be ready");
            }
            Thread.sleep(100);
        }
    }
    
    /**
     * Wait for the specified node server to be registered with the active Grid hub.
     * 
     * @param config {@link SeleniumConfig} object
     * @param nodeServer {@link LocalGridServer node server} to wait for
     * @param maxWait maximum interval in milliseconds to wait; negative interval to wait indefinitely
     * @throws InterruptedException if this thread was interrupted
     * @throws IOException if an I/O error occurs
     * @throws TimeoutException if not waiting indefinitely and exceeded maximum wait
     */
    protected static void waitUntilRegistered(SeleniumConfig config, LocalGridServer nodeServer, long maxWait)
            throws IOException, InterruptedException, TimeoutException {
        String nodeEndpoint = nodeServer.getUrl().getProtocol() + "://" + nodeServer.getUrl().getAuthority();
        long maxTime = System.currentTimeMillis() + maxWait;
        while (!isNodeRegistered(config, nodeEndpoint)) {
            if ((maxWait > 0) && (System.currentTimeMillis() > maxTime)) {
                throw new TimeoutException("Timed out waiting for Grid node to be registered");
            }
            Thread.sleep(100);
        }
        
    }
    
    /**
     * Determine if the specified node server is registered with the active Grid hub.
     * 
     * @param config {@link SeleniumConfig} object
     * @param nodeEndpoint node endpoint
     * @return {@code true} if the node is registered; otherwise {@code false}
     * @throws IOException if an I/O error occurs
     */
    private static boolean isNodeRegistered(SeleniumConfig config, String nodeEndpoint) throws IOException {
        Capabilities capabilities = GridUtility.getNodeCapabilities(config, config.getHubUrl(), nodeEndpoint);
        return capabilities.is("success");
    }

    /**
     * Create an object that represents a Selenium Grid server with the specified arguments.
     * <p>
     * <b>NOTE</b>: The created object defines a separate process for managing the local server, but does <b>NOT</b>
     * start this process.
     * 
     * @param launcherClassName fully-qualified name of {@code GridLauncher} class
     * @param dependencyContexts fully-qualified names of context classes for Selenium Grid dependencies
     * @param role role of Grid server being started
     * @param port port that Grid server should use; 0 to specify auto-configuration
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
    public static LocalGridServer create(final String launcherClassName, final String[] dependencyContexts,
            final GridRole role, final Integer port, final Path configPath, final Path workingPath,
            final Path outputPath, final String... propertyNames) {
        
        String gridRole = role.toString().toLowerCase();
        List<String> argsList = new ArrayList<>();
        
        // specify server role
        argsList.add(OPT_ROLE);
        argsList.add(gridRole);
        
        String hostUrl = GridUtility.getLocalHost();
        
        // specify server host
        argsList.add(OPT_HOST);
        argsList.add(hostUrl);
        
        Integer portNum = port;
        // if port auto-select spec'd
        if (portNum == 0) {
            // acquire available port
            portNum = PortProber.findFreePort();
        }
        
        // specify server port
        argsList.add(OPT_PORT);
        argsList.add(portNum.toString());
        
        // specify server configuration file
        argsList.add("-" + gridRole + "Config");
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
        
        String executable = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        CommandLine process = new CommandLine(executable, argsList.toArray(new String[0]));
        return new LocalGridServer(hostUrl, portNum, role, process, workingPath, outputPath);
    }

    public static class LocalGridServer extends GridServer {

        private final CommandLine process;
        private final Map<String, String> personalities = new HashMap<>();
        
        /**
         * Constructor for local Grid server object.
         * 
         * @param host IP address of local Grid server
         * @param port port of local Grid server
         * @param role {@link GridRole} of local Grid server
         * @param process {@link Process} of local Grid server
         * @param workingPath {@link Path} of working directory for server process; {@code null} for default
         * @param outputPath {@link Path} to output log file; {@code null} to decline log-to-file
         */
        public LocalGridServer(String host, Integer port, GridRole role, CommandLine process, Path workingPath, Path outputPath) {
            super(getServerUrl(host, port), role);
            
            if (workingPath != null) {
                process.setWorkingDirectory(workingPath.toString());
            }
            
            if (outputPath != null) {
                try {
                    process.copyOutputTo(new FileOutputStream(outputPath.toFile()));
                } catch (FileNotFoundException e) {
                    throw new GridServerLaunchFailedException(role.toString().toLowerCase(), e);
                }
            }
            
            this.process = process;
        }
        
        /**
         * Get process for this local Grid server.
         * 
         * @return {@link Process} object
         */
        public CommandLine getProcess() {
            return process;
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
            if (!isActive()) {
                SeleniumConfig config = SeleniumConfig.getConfig();
                long hostTimeout = config.getLong(SeleniumSettings.HOST_TIMEOUT.key()) * 1000;
                
                process.executeAsync();
                waitUntilReady(this, hostTimeout);
                
                if (!isHub()) {
                    waitUntilRegistered(config, this, hostTimeout);
                }
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean shutdown(final boolean localOnly) throws InterruptedException {
            if (isHub()) {
                return super.shutdown(localOnly);
            } else if (isActive()) {
                getProcess().destroy();
            }
            
            return true;
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
                return new URL("http://" + host + ":" + port.toString() + GridServer.HUB_BASE);
            } catch (MalformedURLException e) {
                throw UncheckedThrow.throwUnchecked(e);
            }
        }
    }
}
