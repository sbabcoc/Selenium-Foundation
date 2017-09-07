package com.nordstrom.automation.selenium.core;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.grid.selenium.GridLauncher;
import org.testng.ITestResult;

import com.nordstrom.automation.selenium.SeleniumConfig;

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
class GridProcess {
    
    private static final String OPT_ROLE = "-role";
    private static final Class<?>[] dependencies = { GridLauncher.class };
    
    /**
     * Start a Selenium Grid server with the specified arguments in a separate process.
     * 
     * @param testResult TestNG test results object (may be 'null')
     * @param args Selenium server command line arguments (check {@code See Also} below)
     * @return Java {@link Process} object for managing the server process
     * @throws IOException 
     * @see <a href="http://www.seleniumhq.org/docs/07_selenium_grid.jsp#getting-command-line-help">Getting Command-Line Help<a>
     */
    static Process start(ITestResult testResult, String[] args) throws IOException {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        int optIndex = argsList.indexOf(OPT_ROLE);
        String gridRole = args[optIndex + 1];
        
        argsList.add(0, GridLauncher.class.getName());
        argsList.add(0, getClasspath(dependencies));
        argsList.add(0, "-cp");
        argsList.add(0, System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        
        ProcessBuilder builder = new ProcessBuilder(argsList);
        
        String outputDir = SeleniumConfig.getOutputDir(testResult);
        File outputFile = new File(outputDir, "grid-" + gridRole + ".log");
        
        builder.redirectErrorStream(true);
        builder.redirectOutput(outputFile);
        
        try {
            Files.createDirectories(outputFile.toPath().getParent());
            return builder.start();
        } catch (IOException e) {
            throw new IOException("Failed to start grid " + gridRole + " process", e);
        }
    }
    
    /**
     * Assemble a classpath array from the specified array of dependencies.
     * 
     * @param dependencies array of dependencies
     * @return classpath array
     */
    private static String getClasspath(Class<?>[] dependencies) {
        List<String> pathList = new ArrayList<>();
        for (Class<?> clazz : dependencies) {
            pathList.add(findPathJar(clazz));
        }
        return String.join(File.pathSeparator, pathList);
    }
    
    /**
     * If the provided class has been loaded from a jar file that is on the
     * local file system, will find the absolute path to that jar file.
     * 
     * @param context
     *            The jar file that contained the class file that represents
     *            this class will be found.
     * @throws IllegalStateException
     *             If the specified class was loaded from a directory or in some
     *             other way (such as via HTTP, from a database, or some other
     *             custom class-loading device).
     */
    public static String findPathJar(Class<?> context) throws IllegalStateException {
        String rawName = context.getName();
        String classFileName;
        /* rawName is something like package.name.ContainingClass$ClassName. We need to turn this into ContainingClass$ClassName.class. */ {
            int idx = rawName.lastIndexOf('.');
            classFileName = (idx == -1 ? rawName : rawName.substring(idx+1)) + ".class";
        }

        String uri = context.getResource(classFileName).toString();
        if (uri.startsWith("file:")) throw new IllegalStateException("This class has been loaded from a directory and not from a jar file.");
        if (!uri.startsWith("jar:file:")) {
            int idx = uri.indexOf(':');
            String protocol = idx == -1 ? "(unknown)" : uri.substring(0, idx);
            throw new IllegalStateException("This class has been loaded remotely via the " + protocol +
                    " protocol. Only loading from a jar on the local file system is supported.");
        }

        int idx = uri.indexOf('!');
        //As far as I know, the if statement below can't ever trigger, so it's more of a sanity check thing.
        if (idx == -1) throw new IllegalStateException("You appear to have loaded this class from a local jar file, but I can't make sense of the URL!");

        try {
            String fileName = URLDecoder.decode(uri.substring("jar:file:".length(), idx), Charset.defaultCharset().name());
            return new File(fileName).getAbsolutePath();
        } catch (UnsupportedEncodingException e) {
            throw new InternalError("default charset doesn't exist. Your VM is borked.");
        }
    }
}
