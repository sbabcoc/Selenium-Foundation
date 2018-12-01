package com.nordstrom.automation.selenium.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpHost;
import org.openqa.grid.common.GridRole;
import org.openqa.selenium.net.UrlChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.exceptions.GridServerLaunchFailedException;
import com.nordstrom.common.base.UncheckedThrow;
import com.nordstrom.common.file.PathUtils;

/**
 * This class launches Selenium Grid server instances, each in its own system process. Clients of this class specify
 * the role of the server (either {@code hub} or {@code node}), and they get a {@link Process} object for managing
 * the server lifetime as a result.
 * <p>
 * The output of the process is redirected to a file named <ins>grid-<i>&lt;role&gt;</i>.log</ins> in the test context
 * output directory. Process error output is redirected, so this log file will contain both standard output and errors.
 * <p>
 * <b>NOTE</b>: If no test context is specified, the log file will be stored in the "current" directory of the parent
 * Java process.  
 */
@SuppressWarnings("squid:S1774")
public final class LocalGrid {
    
    private GridServer hubServer;
    private List<GridServer> nodeServers = new ArrayList<>();

    private static final long SHUTDOWN_DELAY = 15;
    private static final String HUB_SHUTDOWN = "/lifecycle-manager?action=shutdown";
    
    private static final String OPT_ROLE = "-role";
    private static final String OPT_PORT = "-port";
    private static final String LOGS_PATH = "logs";
    
    private static final String HUB_READY = "up and running";
    private static final String NODE_READY = "ready to use";
    private static final String GRID_ENDPOINT = "/wd/hub";
    private static final String GRID_REGISTER = "/grid/register";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalGrid.class);
    
    /**
     * Private constructor to prevent direct instantiation from outside.
     */
    private LocalGrid() {
    }
    
    /**
     * Get grid server object for the active hub.
     * 
     * @return {@link GridServer} object that represents the active hub server
     */
    public GridServer getHubServer() {
        return hubServer;
    }
    
    /**
     * Get the list of grid server objects for the attached nodes.
     * 
     * @return list of {@link GridServer} objects that represent the attached node servers
     */
    public List<GridServer> getNodeServers() {
        return nodeServers;
    }
    
    /**
     * 
     * @param config
     * @param hubConfigPath
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public static LocalGrid launch(SeleniumConfig config, final Path hubConfigPath)
                    throws IOException, InterruptedException, TimeoutException {
        
        String launcherClassName = config.getLauncherClassName();
        String[] dependencyContexts = config.getDependencyContexts();
        long hostTimeout = config.getLong(SeleniumSettings.HOST_TIMEOUT.key()) * 1000;
        Integer hubPort = config.getInteger(SeleniumSettings.HUB_PORT.key(), Integer.valueOf(-1));
        GridServer hubServer = start(launcherClassName, dependencyContexts, GridRole.HUB, hubPort, hubConfigPath);
        URL gridHub = waitUntilReady(hubServer, hostTimeout);
        
        System.setProperty(SeleniumSettings.HUB_HOST.key(), gridHub.toString());
        
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

        List<GridServer> nodeServers = new ArrayList<>();
        for (DriverPlugin driverPlugin : ServiceLoader.load(DriverPlugin.class)) {
            String capabilities = driverPlugin.getCapabilities();
            Path nodeConfigPath = config.createNodeConfig(capabilities, gridHub);
            GridServer nodeServer = driverPlugin.start(launcherClassName, dependencyContexts, nodeConfigPath);
            waitUntilReady(nodeServer, hostTimeout);
            nodeServers.add(nodeServer);
        }
        
        LocalGrid localGrid = new LocalGrid();
        localGrid.hubServer = hubServer;
        localGrid.nodeServers = nodeServers;
        
        GridUtility.getGridProxies(gridHub);

        return localGrid;
    }
    
    /**
     * Wait for the specified Grid server to indicate that it's ready.
     * 
     * @param server {@link GridServer} object to wait for.
     * @param maxWait maximum interval in milliseconds to wait; negative interval to wait indefinitely
     * @return all of the input that was received while waiting for the prompt
     * @throws InterruptedException if this thread was interrupted
     * @throws IOException if an I/O error occurs
     * @throws TimeoutException if not waiting indefinitely and exceeded maximum wait
     */
    private static URL waitUntilReady(GridServer server, long maxWait) throws IOException, InterruptedException, TimeoutException {
        boolean didTimeout = false;
        StringBuilder builder = new StringBuilder();
        long maxTime = System.currentTimeMillis() + maxWait;
        try (InputStream inputStream = Files.newInputStream(server.outputPath)) {
            while (appendAndCheckFor(inputStream, server.readyMessage, builder)) {
                if ((maxWait > 0) && (System.currentTimeMillis() > maxTime)) {
                    didTimeout = true;
                    break;
                }
                Thread.sleep(100);
            }
        }
        
        String output = builder.toString();
        System.out.println("output: " + output);
        
        if (!didTimeout) {
            int endIndex = output.indexOf(GRID_REGISTER) + GRID_REGISTER.length();
            int beginIndex = output.lastIndexOf(' ', endIndex) + 1;
            URI gridHub = URI.create(output.substring(beginIndex, endIndex));
            URI localhost = URI.create(getLocalHost());
            return new URL(localhost.getScheme(), localhost.getHost(), gridHub.getPort(), GRID_ENDPOINT);
        }
        
        throw new TimeoutException("Timed of waiting for Grid server to be ready");
    }
    
    /**
     * Append available channel input to the supplied string builder and check for the specified prompt.
     * 
     * @param readyMessage prompt to check for
     * @param builder {@link StringBuilder} object
     * @return 'false' is prompt is found or channel is closed; otherwise 'true'
     * @throws InterruptedException if this thread was interrupted
     * @throws IOException if an I/O error occurs
     */
    private static boolean appendAndCheckFor(InputStream inputStream, String readyMessage, StringBuilder builder) throws InterruptedException, IOException {
        String recv = readAvailable(inputStream);
        if ( ! recv.isEmpty()) {
            builder.append(recv);
            int readyMsgIndex = builder.indexOf(readyMessage);
            int registerIndex = builder.indexOf(GRID_REGISTER);
            return ((readyMsgIndex == -1) || (registerIndex == -1));
        }
        return true;
    }
    
    /**
     * Read available input from the specified input stream.
     * 
     * @param inputStream input stream
     * @return available input
     * @throws IOException if an I/O error occurs
     */
    private static String readAvailable(InputStream inputStream) throws IOException {
        int length;
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }
    
    /**
     * Start a Selenium Grid server with the specified arguments in a separate process.
     * 
     * @param launcherClassName fully-qualified name of {@code GridLauncher} class
     * @param dependencyContexts fully-qualified names of context classes for Selenium Grid dependencies
     * @param role role of Grid server being started
     * @param port port that Grid server should use; -1 to specify auto-configuration
     * @return {@link GridServer} object for managing the server process
     * @throws GridServerLaunchFailedException If a Grid component process failed to start
     * @see <a href="http://www.seleniumhq.org/docs/07_selenium_grid.jsp#getting-command-line-help">
     *      Getting Command-Line Help<a>
     */
    public static GridServer start(final String launcherClassName,
                    final String[] dependencyContexts, final GridRole role, final Integer port, final Path configPath) {
        String gridRole = role.toString().toLowerCase();
        List<String> argsList = new ArrayList<>();
        argsList.add(OPT_ROLE);
        argsList.add(gridRole);
        if (port.intValue() != -1) {
            argsList.add(OPT_PORT);
            argsList.add(port.toString());
        }
        argsList.add("-" + gridRole + "Config");
        argsList.add(configPath.toString());
        
        argsList.add(0, launcherClassName);
        argsList.add(0, getClasspath(dependencyContexts));
        argsList.add(0, "-cp");
        argsList.add(0, System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        
        ProcessBuilder builder = new ProcessBuilder(argsList);
        
        Path outputPath;
        String outputDir = PathUtils.getBaseDir();
        
        try {
            Path logsPath = Paths.get(outputDir, LOGS_PATH);
            if (!logsPath.toFile().exists()) {
                Files.createDirectories(logsPath);
            }
            outputPath = PathUtils.getNextPath(logsPath, "grid-" + gridRole, "log");
        } catch (IOException e) {
            throw new GridServerLaunchFailedException(gridRole, e);
        }
        
        builder.redirectErrorStream(true);
        builder.redirectOutput(outputPath.toFile());
        
        try {
            return new GridServer(gridRole, builder.start(), outputPath);
        } catch (IOException e) {
            throw new GridServerLaunchFailedException(gridRole, e);
        }
    }
    
    /**
     * Assemble a classpath array from the specified array of dependencies.
     * 
     * @param dependencyContexts array of dependency contexts
     * @return classpath array
     */
    public static String getClasspath(final String[] dependencyContexts) {
        Set<String> pathList = new HashSet<>();
        for (String contextClassName : dependencyContexts) {
            pathList.add(findJarPathFor(contextClassName));
        }
        return String.join(File.pathSeparator, pathList);
    }
    
    /**
     * If the provided class has been loaded from a JAR file that is on the
     * local file system, will find the absolute path to that JAR file.
     * 
     * @param contextClassName
     *            The JAR file that contained the class file that represents
     *            this class will be found.
     * @return absolute path to the JAR file from which the specified class was
     *            loaded
     * @throws IllegalStateException
     *           If the specified class was loaded from a directory or in some
     *           other way (such as via HTTP, from a database, or some other
     *           custom class-loading device).
     */
    public static String findJarPathFor(final String contextClassName) {
        Class<?> contextClass;
        
        try {
            contextClass = Class.forName(contextClassName);
        } catch (ClassNotFoundException e) {
            throw UncheckedThrow.throwUnchecked(e);
        }
        
        String shortName = contextClassName;
        int idx = shortName.lastIndexOf('.');
        
        if (idx > -1) {
            shortName = shortName.substring(idx + 1);
        }
        
        String uri = contextClass.getResource(shortName + ".class").toString();
        
        if (uri.startsWith("file:")) {
            throw new IllegalStateException("This class has been loaded from a directory and not from a jar file.");
        }
        
        if (!uri.startsWith("jar:file:")) {
            idx = uri.indexOf(':');
            String protocol = (idx > -1) ? uri.substring(0, idx) : "(unknown)";
            throw new IllegalStateException("This class has been loaded remotely via the " + protocol
                    + " protocol. Only loading from a jar on the local file system is supported.");
        }

        idx = uri.indexOf('!');

        if (idx > -1) {
            try {
                String fileName = URLDecoder.decode(uri.substring("jar:file:".length(), idx),
                                Charset.defaultCharset().name());
                return new File(fileName).getAbsolutePath();
            } catch (UnsupportedEncodingException e) {
                throw new InternalError("Default charset doesn't exist. Your VM is borked.", e);
            }
        }
        
        throw new IllegalStateException(
                "You appear to have loaded this class from a local jar file, but I can't make sense of the URL!");
    }
    
    public static class GridServer {
        private boolean isHub;
        private Process process;
        private Path outputPath;
        private String readyMessage;
        private HttpHost serverHost;
        private String statusRequest;
        private String shutdownRequest;
        
        GridServer(String role, Process process, Path outputPath) {
            this.isHub = ("hub".equals(role));
            this.process = process;
            this.outputPath = outputPath;
            if (isHub) {
                readyMessage = HUB_READY;
            } else {
                readyMessage = NODE_READY;
            }
        }
        
        public boolean isHub() {
            return isHub;
        }
    
        public Process getProcess() {
            return process;
        }

        public Path getOutputPath() {
            return outputPath;
        }

        public String getReadyMessage() {
            return readyMessage;
        }

        public void setReadyMessage(String readyMessage) {
            this.readyMessage = readyMessage;
        }
        
        public boolean stopGridServer(final boolean localOnly) {
            return stopGridServer(serverHost, statusRequest, shutdownRequest, localOnly);
        }
        
        /**
         * Stop the specified Selenium Grid server.
         * 
         * @param serverParms Selenium Grid server parameters
         * @param localOnly 'true' to target only local Grid server
         * @return 'false' if [localOnly] and server is remote; otherwise 'true'
         */
        public static boolean stopGridServer(final HttpHost serverHost, final String statusRequest,
                        final String shutdownRequest, final boolean localOnly) {
            
            if (localOnly && !GridUtility.isLocalHost(serverHost)) {
                return false;
            }
            
            if (GridUtility.isHostActive(serverHost, statusRequest)) {
                try {
                    URL hostUrl = URI.create(serverHost.toURI()).toURL();
                    GridUtility.getHttpResponse(serverHost, shutdownRequest);
                    new UrlChecker().waitUntilUnavailable(SHUTDOWN_DELAY, TimeUnit.SECONDS, hostUrl);
                } catch (IOException | org.openqa.selenium.net.UrlChecker.TimeoutException e) {
                    throw UncheckedThrow.throwUnchecked(e);
                }
            }
            
            return true;
        }
    }

    /**
     * Get Internet protocol (IP) address for the machine we're running on.
     * 
     * @return IP address for the machine we're running on (a.k.a. - 'localhost')
     */
    public static String getLocalHost() {
        SeleniumConfig config = AbstractSeleniumConfig.getConfig();
        String host = config.getString(SeleniumSettings.GOOGLE_DNS_SOCKET_HOST.key());
        int port = config.getInt(SeleniumSettings.GOOGLE_DNS_SOCKET_PORT.key());
        
        try (final DatagramSocket socket = new DatagramSocket()) {
            // use Google Public DNS to discover preferred local IP
            socket.connect(InetAddress.getByName(host), port);
            return socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException eaten) { //NOSONAR
            LOGGER.warn("Unable to get 'localhost' IP address: {}", eaten.getMessage());
            return "localhost";
        }
    }

}
