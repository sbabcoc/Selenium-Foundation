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
import org.openqa.selenium.os.CommandLine;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.core.LocalSeleniumGrid.LocalGridServer;
import com.nordstrom.automation.selenium.core.SeleniumGrid.GridServer;
import com.nordstrom.automation.selenium.exceptions.GridServerLaunchFailedException;
import com.nordstrom.automation.selenium.utility.BinaryFinder;

import net.bytebuddy.implementation.Implementation;

/**
 * This class provides the base plugin implementation for drivers provided by {@code appium}.
 */
public abstract class AbstractAppiumPlugin implements DriverPlugin {

    private static final String[] DEPENDENCY_CONTEXTS = {};
    private static final String[] APPIUM_PATH_TAIL = { "appium", "build", "lib", "main.js" };
    private static final String[] PROPERTY_NAMES = {};
    
    private String browserName;
    
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
    public String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalGridServer start(SeleniumConfig config, String launcherClassName, String[] dependencyContexts,
            GridServer hubServer, Path workingPath, Path outputPath) throws IOException {
        
        String capabilities = getCapabilities(config);
        Path nodeConfigPath = config.createNodeConfig(capabilities, hubServer.getUrl());
        
        List<String> argsList = new ArrayList<>();

        argsList.add(findMainScript().getAbsolutePath());
        
        String hostUrl = GridUtility.getLocalHost();
        int port = 0;
        
        // specify server host
        argsList.add("--address");
        argsList.add(hostUrl);
        
        Integer portNum = port;
        // if port auto-select spec'd
        if (portNum.intValue() == 0) {
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
        
        String executable = findNodeBinary().getAbsolutePath();
        CommandLine process = new CommandLine(executable, argsList.toArray(new String[0]));
        return new LocalGridServer(hostUrl, portNum, GridRole.NODE, process, workingPath, outputPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Implementation getWebElementCtor(WebDriver driver, Class<? extends WebElement> refClass) {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * Find the 'npm' (Node Package Manager) binary.
     * 
     * @return path to the 'npm' binary as a {@link File} object
     * @throws GridServerLaunchFailedException if 'npm' isn't found
     */
    private static File findNPM() throws GridServerLaunchFailedException {
        return findBinary("npm", SeleniumSettings.NPM_BINARY_PATH, "'npm' package manager");
    }
    
    /**
     * Find the 'node' binary.
     * 
     * @return path to the 'node' binary as a {@link File} object
     * @throws GridServerLaunchFailedException if 'npm' isn't found
     */
    private static File findNodeBinary() throws GridServerLaunchFailedException {
        return findBinary("node", SeleniumSettings.NODE_BINARY_PATH, "'node' JavaScript runtime");
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
            throw fileNotFound("'appium' main script", SeleniumSettings.APPIUM_BINARY_PATH);
        } catch (IOException cause) {
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
            return BinaryFinder.findBinary(exeName, setting.key(), null, null);
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

}
