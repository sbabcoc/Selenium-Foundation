package com.nordstrom.automation.selenium.utility;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.manager.SeleniumManager;
import org.openqa.selenium.manager.SeleniumManagerOutput.Result;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.common.file.PathUtils;

/**
 * This static utility class provides methods to find/validate drivers and executable binary files.
 */
public class BinaryFinder {

    /**
     * Private constructor to prevent instantiation.
     */
    private BinaryFinder() {
        throw new AssertionError("BinaryFinder is a static utility class that cannot be instantiated");
    }
    
    /**
     * Find/install driver indicated by the specified capabilities.
     * 
     * @param capabilities For driver binaries, the required capabilities for the specified driver
     * @return path to driver supporting specified capabilities as a {@link File} object
     */
    public static File findDriver(String capabilities) {
        Capabilities caps = SeleniumConfig.getConfig().getCapabilitiesForJson(capabilities)[0];
        SeleniumManager manager = SeleniumManager.getInstance();
        Result result = manager.getBinaryPaths(new ArrayList<String>(Arrays.asList("--browser", caps.getBrowserName())));
        return new File(result.getDriverPath());
    }

    /**
    * Find the specified executable file.
    * 
    * @param exeName Name of the executable file to look for in PATH
    * @param exeProperty Name of a system property that specifies the path to the executable file
    * @return The specified executable as a {@link File} object
    * @throws IllegalStateException if the executable is not found or cannot be executed
    */
    public static File findBinary(String exeName, String exeProperty) {
        String defaultPath = PathUtils.findExecutableOnSystemPath(exeName);
        String exePath = System.getProperty(exeProperty, defaultPath);
        checkState(exePath != null,
                "The path to the driver executable must be set by the %s system property",
                exeProperty);

        File exe = new File(exePath);
        checkExecutable(exe);
        return exe;
    }

    /**
     * Ensure that the specified object exists as an executable file.
     * 
     * @param exe executable to check as a {@link File} object
     * @throws IllegalStateException if the executable is not found or cannot be executed
     */
    protected static void checkExecutable(File exe) {
        checkState(exe.exists(), "Specified file does not exist: %s", exe.getAbsolutePath());
        checkState(!exe.isDirectory(), "Specified file is a directory: %s", exe.getAbsolutePath());
        checkState(exe.canExecute(), "Specified file is not executable: %s", exe.getAbsolutePath());
    }

}
