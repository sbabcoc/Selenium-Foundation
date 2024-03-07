package com.nordstrom.automation.selenium.utility;

import java.io.File;
import java.io.IOException;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.service.DriverService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.nordstrom.automation.selenium.SeleniumConfig;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * This class extends {@link DriverService} to provide access to the
 * {@link DriverService#findExecutable(String, String, String, String) findExecutable} method.
 */
public class BinaryFinder extends DriverService {

    private BinaryFinder(File executable, int port, ImmutableList<String> args,
            ImmutableMap<String, String> environment) throws IOException {
        super(executable, port, args, environment);
    }
    
    /**
     * Find/install driver indicated by the specified capabilities.
     * 
     * @param capabilities For driver binaries, the required capabilities for the specified driver
     * @return path to driver supporting specified capabilities as a {@link File} object
     */
    public static File findDriver(String capabilities) {
        Capabilities caps = SeleniumConfig.getConfig().getCapabilitiesForJson(capabilities)[0];
        WebDriverManager manager = WebDriverManager.getInstance(caps.getBrowserName()).capabilities(caps);
        manager.setup();
        return new File(manager.getDownloadedDriverPath());
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
        return DriverService.findExecutable(exeName, exeProperty, null, null);
    }

}
