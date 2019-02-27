package com.nordstrom.automation.selenium.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.openqa.grid.common.GridRole;
import org.openqa.grid.web.servlet.LifecycleServlet;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.net.PortProber;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.exceptions.GridServerLaunchFailedException;
import com.nordstrom.common.base.UncheckedThrow;
import com.nordstrom.common.file.PathUtils;

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

    private Map<String, String> personalities;
    
    private static final String OPT_ROLE = "-role";
    private static final String OPT_HOST = "-host";
    private static final String OPT_PORT = "-port";
    private static final String OPT_SERVLETS = "-servlets";
    private static final String LOGS_PATH = "logs";
    
    private static final String GRID_REGISTER = "/grid/register";
    
    public LocalSeleniumGrid(Map<String, String> personalities, LocalGridServer hubServer, LocalGridServer... nodeServers) {
        super(hubServer, nodeServers);
        this.personalities = personalities;
    }
    
    /* 
     * TODO Add constructor to re-constitute local Grid from active hub.
     * 
     * => Get capabilities of active node:  
     *    Capabilities[] nodeCaps = GridUtility.getNodeCapabilities(config, hubUrl, nodeEndpoint);
     *    
     * => Match capabilities with configured browser plug-in:
     *    for (DriverPlugin driverPlugin : ServiceLoader.load(DriverPlugin.class)) {
     *        // determine if node browser name(s) == plug-in browser name(s)
     *    }
     */
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Capabilities getPersonality(SeleniumConfig config, String personality) {
        String json = personalities.get(personality);
        if ((json == null) || json.isEmpty()) {
            String browserName = personality.split("\\.")[0];
            LOGGER.warn("Specified personality '{}' not supported by local Grid; revert to browser name '{}'",
                            personality, browserName);
            return super.getPersonality(config, browserName);
        } else {
            return config.getCapabilitiesForJson(json)[0];
        }
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
        long hostTimeout = config.getLong(SeleniumSettings.HOST_TIMEOUT.key()) * 1000;
        Integer hubPort = config.getInteger(SeleniumSettings.HUB_PORT.key(), Integer.valueOf(-1));
        LocalGridServer hubServer = start(launcherClassName, dependencyContexts, GridRole.HUB, hubPort, hubConfigPath);
        waitUntilReady(hubServer, hostTimeout);
        
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
        Map<String, String> personalities = new HashMap<>();
        for (DriverPlugin driverPlugin : ServiceLoader.load(DriverPlugin.class)) {
            LocalGridServer nodeServer = driverPlugin.start(config, launcherClassName, dependencyContexts, hubServer);
            waitUntilReady(nodeServer, hostTimeout);
            nodeServers.add(nodeServer);
            
            Map<String, String> nodeCaps = driverPlugin.getPersonalities();
            if (nodeCaps != null) {
                personalities.putAll(nodeCaps);
            }
        }
        
        return new LocalSeleniumGrid(personalities, hubServer, nodeServers.stream().toArray(LocalGridServer[]::new));
    }

    /**
     * Wait for the specified Grid server to indicate that it's ready.
     * 
     * @param server {@link LocalGridServer} object to wait for.
     * @param maxWait maximum interval in milliseconds to wait; negative interval to wait indefinitely
     * @throws InterruptedException if this thread was interrupted
     * @throws IOException if an I/O error occurs
     * @throws TimeoutException if not waiting indefinitely and exceeded maximum wait
     */
    protected static void waitUntilReady(LocalGridServer server, long maxWait) throws IOException, InterruptedException, TimeoutException {
        StringBuilder builder = new StringBuilder();
        long maxTime = System.currentTimeMillis() + maxWait;
        try (InputStream inputStream = Files.newInputStream(server.outputPath)) {
            while (appendAndCheckFor(inputStream, server.readyMessage, builder)) {
                if ((maxWait > 0) && (System.currentTimeMillis() > maxTime)) {
                    throw new TimeoutException("Timed of waiting for Grid server to be ready");
                }
                Thread.sleep(100);
            }
        }
    }

    /**
     * Append available channel input to the supplied string builder and check for the specified prompt.
     * 
     * @param inputStream {@link InputStream} from which input is read
     * @param readyMessage prompt to check for
     * @param builder {@link StringBuilder} object to which input is appended
     * @return {@code false} is prompt is found or channel is closed; otherwise {@code true}
     * @throws IOException if an I/O error occurs
     */
    protected static boolean appendAndCheckFor(InputStream inputStream, String readyMessage, StringBuilder builder) throws IOException {
        String recv = GridUtility.readAvailable(inputStream);
        if ( ! recv.isEmpty()) {
            builder.append(recv);
            int readyMsgIndex = builder.indexOf(readyMessage);
            int registerIndex = builder.indexOf(GRID_REGISTER);
            return ((readyMsgIndex == -1) || (registerIndex == -1));
        }
        return true;
    }

    /**
     * Start a Selenium Grid server with the specified arguments in a separate process.
     * 
     * @param launcherClassName fully-qualified name of {@code GridLauncher} class
     * @param dependencyContexts fully-qualified names of context classes for Selenium Grid dependencies
     * @param role role of Grid server being started
     * @param port port that Grid server should use; -1 to specify auto-configuration
     * @param configPath {@link Path} to server configuration file
     * @return {@link LocalGridServer} object for managing the server process
     * @throws GridServerLaunchFailedException If a Grid component process failed to start
     * @see <a href="http://www.seleniumhq.org/docs/07_selenium_grid.jsp#getting-command-line-help">
     *      Getting Command-Line Help</a>
     */
    public static LocalGridServer start(final String launcherClassName,
                    final String[] dependencyContexts, final GridRole role, final Integer port,
                    final Path configPath) {
        String gridRole = role.toString().toLowerCase();
        List<String> argsList = new ArrayList<>();
        
        // specify server role
        argsList.add(OPT_ROLE);
        argsList.add(gridRole);
        
        // if starting a Grid node
        if (role == GridRole.NODE) {
            // add lifecycle servlet
            argsList.add(OPT_SERVLETS);
            argsList.add(LifecycleServlet.class.getName());
        }
        
        String hostUrl = GridUtility.getLocalHost();
        
        // specify server host
        argsList.add(OPT_HOST);
        argsList.add(hostUrl);
        
        Integer portNum = port;
        // if port auto-select spec'd
        if (portNum.intValue() == -1) {
            // acquire available port
            portNum = Integer.valueOf(PortProber.findFreePort());
        }
        
        // specify server port
        argsList.add(OPT_PORT);
        argsList.add(portNum.toString());
        
        // specify server configuration file
        argsList.add("-" + gridRole + "Config");
        argsList.add(configPath.toString());
        
        // specify Grid launcher class name
        argsList.add(0, launcherClassName);
        
        // specify Java class path
        argsList.add(0, getClasspath(dependencyContexts));
        argsList.add(0, "-cp");
        
        // specify Java command
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
            return new LocalGridServer(hostUrl, portNum, role, builder.start(), outputPath);
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

    public static class LocalGridServer extends GridServer {

        private Process process;
        Path outputPath;
        String readyMessage;
        
        private static final String HUB_READY = "up and running";
        private static final String NODE_READY = "ready to use";
        
        LocalGridServer(String host, Integer port, GridRole role, Process process, Path outputPath) {
            super(getServerUrl(host, port), role);
            this.process = process;
            this.outputPath = outputPath;
            if (isHub()) {
                readyMessage = HUB_READY;
            } else {
                readyMessage = NODE_READY;
            }
        }
        
        /**
         * Get process for this local Grid server.
         * 
         * @return {@link Process} object
         */
        public Process getProcess() {
            return process;
        }
        
        /**
         * Get the output path for the process of this local Grid server.
         * 
         * @return {@link Path} for process output
         */
        public Path getOutputPath() {
            return outputPath;
        }
        
        /**
         * Get "ready" message for this local Grid server.
         * 
         * @return server "ready" message
         */
        public String getReadyMessage() {
            return readyMessage;
        }
        
        /**
         * Get {@code localhost} URL for Selenium Grid server at the specified port.
         * <p>
         * <b>NOTE</b>: The assembled URL will include the Grid web service base path.
         * 
         * @param port desired server port
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
