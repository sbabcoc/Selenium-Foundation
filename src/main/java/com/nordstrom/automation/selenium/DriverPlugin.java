package com.nordstrom.automation.selenium;

import java.util.List;

import org.openqa.selenium.Capabilities;

public interface DriverPlugin {

    /**
     * Get fully-qualified names of context classes for Selenium Grid dependencies.
     * 
     * @return context class names for Selenium Grid dependencies
     */
    public String[] getDependencyContexts();
    
    public List<Capabilities> getCapabilitiesList();
    
    public default boolean doLaunchStandAlone() {
        return false;
    }
    
    public GridServer launchNode();
    
    public static class GridServer {
        private String name;
        private int port;
        private Process process;
        
        GridServer(String name, int port, Process process) {
            this.name = name;
            this.port = port;
            this.process = process;
        }

        public String getName() {
            return name;
        }

        public int getPort() {
            return port;
        }

        public Process getProcess() {
            return process;
        }
    }
}
