package com.nordstrom.automation.selenium.core;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public final class GridProcess {
    
    public static class GridServer {
        private String name;
        private int port;
        private Process process;
        
        GridServer(String name, int port, Process process) {
            this.name = name;
            this.port = port;
            this.process = process;
        }
    
        public String getName() {
            return name;
        }
    
        public int getPort() {
            return port;
        }
    
        public Process getProcess() {
            return process;
        }
    }

    private static final String OPT_ROLE = "-role";
    private static final String LOGS_PATH = "logs";
    
    /**
     * Private constructor to prevent instantiation.
     */
    private GridProcess() {
        throw new AssertionError("GridProcess is a static utility class that cannot be instantiated");
    }
    
    /**
     * Start a Selenium Grid server with the specified arguments in a separate process.
     * 
     * @param launcherClassName fully-qualified name of {@code GridLauncher} class
     * @param dependencyContexts fully-qualified names of context classes for Selenium Grid dependencies
     * @param args Selenium server command line arguments (check {@code See Also} below)
     * @return Java {@link Process} object for managing the server process
     * @throws GridServerLaunchFailedException If a Grid component process failed to start
     * @see <a href="http://www.seleniumhq.org/docs/07_selenium_grid.jsp#getting-command-line-help">
     *      Getting Command-Line Help<a>
     */
    public static GridServer start(final String launcherClassName, final String[] dependencyContexts, final String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        int optIndex = argsList.indexOf(OPT_ROLE);
        String gridRole = args[optIndex + 1];
        
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
            Process process = builder.start();
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
}
