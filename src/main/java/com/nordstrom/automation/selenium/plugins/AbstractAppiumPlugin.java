package com.nordstrom.automation.selenium.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.grid.common.GridRole;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.service.DriverService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.core.LocalSeleniumGrid.LocalGridServer;
import com.nordstrom.automation.selenium.core.SeleniumGrid.GridServer;
import com.nordstrom.automation.selenium.exceptions.GridServerLaunchFailedException;

import net.bytebuddy.implementation.Implementation;

public abstract class AbstractAppiumPlugin implements DriverPlugin {

    private static final String[] DEPENDENCY_CONTEXTS = {};
    private static final String[] APPIUM_PATH_TAIL = { "appium", "build", "lib", "main.js" };
    private static final String[] PROPERTY_NAMES = {};
    
    private String browserName;
    
    protected AbstractAppiumPlugin(String browserName) {
        this.browserName = browserName;
    }
    
    @Override
    public String[] getDependencyContexts() {
        return DEPENDENCY_CONTEXTS;
    }

    @Override
    public String getBrowserName() {
        return browserName;
    }

    @Override
    public String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

    @Override
    public LocalGridServer start(SeleniumConfig config, String launcherClassName, String[] dependencyContexts,
            GridServer hubServer, Path outputPath) throws IOException {
        
        String capabilities = getCapabilities(config);
        Path nodeConfigPath = config.createNodeConfig(capabilities, hubServer.getUrl());
        
        GridRole role = GridRole.NODE;
        String gridRole = role.toString().toLowerCase();
        List<String> argsList = new ArrayList<>();

        argsList.add(findNodeBinary().getAbsolutePath());
        argsList.add(findMainScript().getAbsolutePath());
        
        String hostUrl = GridUtility.getLocalHost();
        int port = -1;
        
        // specify server host
        argsList.add("--address");
        argsList.add(hostUrl);
        
        Integer portNum = port;
        // if port auto-select spec'd
        if (portNum.intValue() == -1) {
            // acquire available port
            portNum = Integer.valueOf(PortProber.findFreePort());
        }
        
        // specify server port
        argsList.add("--port");
        argsList.add(portNum.toString());
        
        String cliArgs = config.getString(SeleniumSettings.APPIUM_CLI_ARGS.key());
        if (cliArgs != null) {
            argsList.add(cliArgs);
        }
        
        argsList.add("--nodeconfig");
        argsList.add(nodeConfigPath.toString());
        
        ProcessBuilder builder = new ProcessBuilder(argsList);
        builder.redirectErrorStream(true);
        
        if (outputPath != null) {
            builder.redirectOutput(outputPath.toFile());
        }
        
        try {
            return new AppiumGridServer(hostUrl, portNum, role, builder.start());
        } catch (IOException e) {
            throw new GridServerLaunchFailedException(gridRole, e);
        }
    }

    @Override
    public Implementation getWebElementCtor(WebDriver driver, Class<? extends WebElement> refClass) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public static class AppiumGridServer extends LocalGridServer {

        // Create subclass of LocalGridServer.
        // Override default status request string and shutdown(...) method
        
        AppiumGridServer(String host, Integer port, GridRole role, Process process) {
            super(host, port, role, process);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean shutdown(boolean localOnly) throws InterruptedException {
            return super.shutdown(localOnly);
        }
    }
    
    private static File findNPM() throws GridServerLaunchFailedException {
        return findBinary("npm", SeleniumSettings.NPM_BINARY_PATH, "'npm' package manager");
    }
    
    private static File findNodeBinary() throws GridServerLaunchFailedException {
        return findBinary("node", SeleniumSettings.NODE_BINARY_PATH, "'node' JavaScript runtime");
    }
    
    /**
     * Find the 'appium' main script in the global 'node' modules repository.
     * 
     * @return
     * @throws IOException 
     */
    private static File findMainScript() throws GridServerLaunchFailedException {
        try {
            return findBinary("main.js", SeleniumSettings.MAIN_SCRIPT_PATH, "'appium' main script");
        } catch (GridServerLaunchFailedException eaten) {
            // nothing to go here
        }
        
        String nodeModulesRoot;
        File npm = findNPM().getAbsoluteFile();
        
        List<String> argsList = new ArrayList<>();
        
        if (SystemUtils.IS_OS_WINDOWS) {
            argsList.add("cmd.exe");
            argsList.add("/c");
        }
        
        argsList.add(npm.getName());
        argsList.add("root");
        argsList.add("-g");
        
        ProcessBuilder builder = new ProcessBuilder(argsList);
        builder.directory(npm.getParentFile());
        
        try {
            nodeModulesRoot = IOUtils.toString(builder.start().getInputStream(), StandardCharsets.UTF_8).trim();
            File appiumMain = Paths.get(nodeModulesRoot, APPIUM_PATH_TAIL).toFile();
            if (appiumMain.exists()) return appiumMain;
            throw fileNotFound("'appium' main script", SeleniumSettings.MAIN_SCRIPT_PATH);
        } catch (IOException cause) {
            throw new GridServerLaunchFailedException("node", cause);
        }
    }
    
    /**
     * 
     * @param exeName
     * @param setting
     * @param what
     * @return
     * @throws GridServerLaunchFailedException
     */
    private static File findBinary(String exeName, SeleniumSettings setting, String what)
            throws GridServerLaunchFailedException {
        try {
            return BinaryFinder.findBinary(exeName, setting.key(), null, null);
        } catch (IllegalStateException eaten) {
            IOException cause = fileNotFound(what, setting);
            throw new GridServerLaunchFailedException("node", cause);
        }
    }
    
    /**
     * 
     * @param what
     * @param setting
     * @return
     */
    private static IOException fileNotFound(String what, SeleniumSettings setting) {
        String template = "%s not found; configure the %s setting (key: %s)";
        return new FileNotFoundException(String.format(template, what, setting.name(), setting.key()));
    }
    
    static class BinaryFinder extends DriverService {

        private BinaryFinder(File executable, int port, ImmutableList<String> args,
                ImmutableMap<String, String> environment) throws IOException {
            super(executable, port, args, environment);
        }

        /**
        *
        * @param exeName Name of the executable file to look for in PATH
        * @param exeProperty Name of a system property that specifies the path to the executable file
        * @param exeDocs The link to the driver documentation page
        * @param exeDownload The link to the driver download page
        *
        * @return The driver executable as a {@link File} object
        * @throws IllegalStateException if the executable is not found or cannot be executed
        */
        static File findBinary(String exeName, String exeProperty, String exeDocs, String exeDownload) {
            return DriverService.findExecutable(exeName, exeProperty, exeDocs, exeDownload);
        }

    }

}
