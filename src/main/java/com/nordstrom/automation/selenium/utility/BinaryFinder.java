package com.nordstrom.automation.selenium.utility;

import java.io.File;
import java.io.IOException;

import org.openqa.selenium.remote.service.DriverService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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
    * Find the specified executable file.
    * 
    * @param exeName Name of the executable file to look for in PATH
    * @param exeProperty Name of a system property that specifies the path to the executable file
    * @param exeDocs The link to the driver documentation page
    * @param exeDownload The link to the driver download page
    *
    * @return The driver executable as a {@link File} object
    * @throws IllegalStateException if the executable is not found or cannot be executed
    */
    public static File findBinary(String exeName, String exeProperty, String exeDocs, String exeDownload) {
        return DriverService.findExecutable(exeName, exeProperty, exeDocs, exeDownload);
    }

}