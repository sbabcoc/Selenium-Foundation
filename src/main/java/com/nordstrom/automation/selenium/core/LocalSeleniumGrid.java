package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeoutException;
import org.openqa.grid.common.GridRole;
import org.openqa.selenium.net.PortProber;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.exceptions.GridServerLaunchFailedException;
import com.nordstrom.common.base.UncheckedThrow;

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

    public LocalSeleniumGrid(SeleniumConfig config, LocalGridServer hubServer, LocalGridServer... nodeServers) throws IOException {
        super(config, hubServer, nodeServers);
    }
    
    /**
     * Launch local Selenium Grid instance.
     * <p>
     * <b>NOTE</b>: This method stores the hub host URL in the {@link SeleniumSettings#HUB_HOST HUB_HOST} property for
     * subsequent retrieval.
     * 
     * @param config {@link SeleniumConfig} object
     * @param hubConfigPath Selenium Grid hub configuration path
     * @return {@link SeleniumGrid} object for local Grid
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if this thread was interrupted
     * @throws TimeoutException if host timeout interval exceeded
     */
    public static SeleniumGrid launch(SeleniumConfig config, final Path hubConfigPath)
                    throws IOException, InterruptedException, TimeoutException {
        
        String launcherClassName = config.getString(SeleniumSettings.GRID_LAUNCHER.key());
        String[] dependencyContexts = config.getDependencyContexts();
//      long hostTimeout = config.getLong(SeleniumSettings.HOST_TIMEOUT.key()) * 1000;
        Integer hubPort = config.getInteger(SeleniumSettings.HUB_PORT.key(), Integer.valueOf(0));
        Path outputPath = GridUtility.getOutputPath(config, GridRole.HUB);
        LocalGridServer hubServer = start(launcherClassName, dependencyContexts, GridRole.HUB,
                        hubPort, hubConfigPath, outputPath);
        
        // store hub host URL in system property for subsequent retrieval
        System.setProperty(SeleniumSettings.HUB_HOST.key(), hubServer.getUrl().toString());
        
        // two flavors of nodes: standalone (e.g. - appium) or hosted (e.g. - chrome)
        // => bury the distinction by providing a 'start()' method that returns a GridServer object
        // => provide interface method to create capabilities list from JSON string
        //    ... createCapabilitiesList(String jsonStr)
        // => provide configuration interface method to create node configuration files from capabilities lists
        //    ... createNodeConfig(List<Capabilities>)
        //    ... file name: "nodeConfig-<apiVer>-<hashCode>.json"
        //    ... code will check for existing file
        
        // use native implementation to assemble node configuration
        // add interface methods to AbstractSeleniumConfig to manipulate config as JSON:
        // - load node configuration file
        // - replace "capabilities" property with empty list
        // - add capability object to "capabilities" property
        // - serialize JSON object to file
        
        // s2: RegistrationRequest.loadFromJSON(String filePath)
        //     RegistrationRequest.setCapabilities(List<DesiredCapabilities>)
        //     RegistrationRequest.toJson()
        
        // s3: GridNodeConfiguration.loadFromJSON(String filePath)
        //     NOTE: GridNodeConfiguration.capabilities is public
        //     Json.toJson(Object toConvert)
    
        List<LocalGridServer> nodeServers = new ArrayList<>();
        for (DriverPlugin driverPlugin : ServiceLoader.load(DriverPlugin.class)) {
            outputPath = GridUtility.getOutputPath(config, GridRole.NODE);
            LocalGridServer nodeServer = driverPlugin.start(config, launcherClassName, dependencyContexts, hubServer, outputPath);
            nodeServers.add(nodeServer);
        }
        
        return new LocalSeleniumGrid(config, hubServer, nodeServers.toArray(new LocalGridServer[0]));
    }

    /**
     * Start a Selenium Grid server with the specified arguments in a separate process.
     * 
     * @param launcherClassName fully-qualified name of {@code GridLauncher} class
     * @param dependencyContexts fully-qualified names of context classes for Selenium Grid dependencies
     * @param gridRole role of Grid server being started
     * @param port port that Grid server should use; -1 to specify auto-configuration
     * @param configPath {@link Path} to server configuration file
     * @param workingPath {@link Path} of working directory for server process; {@code null} for default
     * @param outputPath {@link Path} to output log file; {@code null} to decline log-to-file
     * @param propertyNames optional array of property names to propagate to server process
     * @return {@link LocalGridServer} object for managing the server process
     * @throws GridServerLaunchFailedException If a Grid component process failed to start
     * @see <a href="http://www.seleniumhq.org/docs/07_selenium_grid.jsp#getting-command-line-help">
     *      Getting Command-Line Help</a>
     */
    public static LocalGridServer start(final String launcherClassName, final String[] dependencyContexts,
            final GridRole gridRole, final Integer port, final Path configPath,
            final Path outputPath, final String... propertyNames) throws IOException {
        
        LocalGridDriverService.Builder builder = 
                new LocalGridDriverService.Builder(launcherClassName, dependencyContexts, gridRole, configPath, propertyNames);
        
        builder.usingPort((port == 0) ? PortProber.findFreePort() : port);
        
        if (outputPath != null) {
            builder.withLogFile(outputPath.toFile());
        }
        
        try {
            return new LocalGridServer(builder);
        } catch (IOException e) {
            throw new GridServerLaunchFailedException(gridRole.toString().toLowerCase(), e);
        }
    }

    public static class LocalGridServer extends GridServer {

        private LocalGridDriverService service;
        
        /**
         * Constructor for local Grid server object.
         * 
         * @param host IP address of local Grid server
         * @param port port of local Grid server
         * @param role {@link GridRole} of local Grid server
         * @param process {@link Process} of local Grid server
         * @throws IOException 
         */
        protected LocalGridServer(LocalGridDriverService.Builder builder) throws IOException {
            super(getServerUrl(builder.getHost(), builder.getPort()), builder.getRole());
            this.service = builder.build();
            this.service.start();
        }
        
        public LocalGridDriverService getService() {
            return service;
        }
        
        /**
         * Get {@code localhost} URL for Selenium Grid server at the specified port.
         * <p>
         * <b>NOTE</b>: The assembled URL will include the Grid web service base path.
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
