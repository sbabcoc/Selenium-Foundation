package com.nordstrom.automation.selenium.utility;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.manager.SeleniumManager;
import org.openqa.selenium.manager.SeleniumManagerOutput.Result;
import org.openqa.selenium.os.ExecutableFinder;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class BinaryFinder {

    /**
     * Find/install driver indicated by the specified capabilities.
     * 
     * @param capabilities For driver binaries, the required capabilities for the specified driver
     * @return path to driver supporting specified capabilities as a {@link File} object
     */
    public static File findDriver(String capabilities) {
        Capabilities caps = SeleniumConfig.getConfig().getCapabilitiesForJson(capabilities)[0];
        SeleniumManager manager = SeleniumManager.getInstance();
        Result result = manager.getDriverPath(caps, false);
        return new File(result.getDriverPath());
    }

    /**
    * Find the specified executable file.
    * 
    * @param exeName Name of the executable file to look for in PATH
    * @param exeProperty Name of a system property that specifies the path to the executable file
    *
    * @return The specified executable as a {@link File} object
    * @throws IllegalStateException if the executable is not found or cannot be executed
    */
    public static File findBinary(String exeName, String exeProperty) {
        String defaultPath = new ExecutableFinder().find(exeName);
        String exePath = System.getProperty(exeProperty, defaultPath);
        checkState(exePath != null,
                "The path to the driver executable must be set by the %s system property",
                exeProperty);

        File exe = new File(exePath);
        checkExecutable(exe);
        return exe;
    }

    protected static void checkExecutable(File exe) {
        checkState(exe.exists(), "The driver executable does not exist: %s", exe.getAbsolutePath());
        checkState(!exe.isDirectory(), "The driver executable is a directory: %s", exe.getAbsolutePath());
        checkState(exe.canExecute(), "The driver is not executable: %s", exe.getAbsolutePath());
    }

}
