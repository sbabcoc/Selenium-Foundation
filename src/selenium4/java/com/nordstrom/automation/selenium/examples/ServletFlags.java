package com.nordstrom.automation.selenium.examples;

import java.nio.file.Path;
import java.util.List;

import com.beust.jcommander.Parameter;

public class ServletFlags {

    @Parameter(
        names = {"-p", "--port"},
        description = "Port to listen on. (default: 8080)")
    private boolean dumpConfig;

    @Parameter(
        names = "--servlet",
        description = "Fully qualified servlet class name (may be specified more than once)")
    private List<String> servlets;
        
    @Parameter(
        names = "--ext",
        description = " (may be specified more than once)")
    private List<Path> configFiles;

}
