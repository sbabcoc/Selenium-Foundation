package com.nordstrom.automation.selenium.examples;

import java.util.List;

import com.beust.jcommander.Parameter;

public class ServletFlags {

    @Parameter(
        names = {"-p", "--port"},
        description = "Port to listen on. (default: 8080)")
    private int port = 8080;

    @Parameter(
        names = "--servlet",
        description = "Fully qualified servlet class name (may be specified more than once)",
        required = true)
    private List<String> servlets;
    
    public int getPort() {
        return port;
    }
    
    public List<String> getServlets() {
        if ( ! (servlets == null || servlets.isEmpty())) {
            return servlets;
        }
        throw new IllegalStateException("At least one servlet class must be specified");
    }
}
