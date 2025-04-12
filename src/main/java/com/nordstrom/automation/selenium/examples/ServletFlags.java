package com.nordstrom.automation.selenium.examples;

import java.util.List;

import com.beust.jcommander.Parameter;

/**
 * This class defines the options supported by the {@link ServletContainer} command line utility.
 */
public class ServletFlags {

    /** servlet container port */
    @Parameter(
        names = {"-p", "--port"},
        description = "Port to listen on. (default: 8080)")
    private int port = 8080;

    /** hosted servlet classes */
    @Parameter(
        names = "--servlet",
        description = "Fully qualified servlet class name (may be specified more than once)",
        required = true)
    private List<String> servlets;
    
    /**
     * Get servlet container port.
     * 
     * @return servlet container port
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Get hosted servlet classes.
     * 
     * @return hosted servlet classes
     */
    public List<String> getServlets() {
        if ( ! (servlets == null || servlets.isEmpty())) {
            return servlets;
        }
        throw new IllegalStateException("At least one servlet class must be specified");
    }
}
