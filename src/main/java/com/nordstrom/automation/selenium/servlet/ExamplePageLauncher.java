package com.nordstrom.automation.selenium.servlet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.core.ServerProcessKiller;
import com.nordstrom.automation.selenium.examples.ServletContainer;
import com.nordstrom.automation.selenium.utility.HostUtils;
import com.nordstrom.common.file.PathUtils;
import com.nordstrom.common.jar.JarUtils;

/**
 * This class is a thin wrapper around the example page servlet launcher.
 */
public class ExamplePageLauncher {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExamplePageLauncher.class);
    
    /**
     * This enumeration implements the launcher for the local example page servlet used by the
     * <b>Selenium Foundation</b> unit tests.
     */
    public enum Container {
        /** singleton instance of example page launcher */
        INSTANCE;
        
        private final ProcessBuilder builder;
        private Process process;
        private boolean hasStarted = false;
        private boolean isActive = false;
        
        private Container() {
            List<String> argsList = new ArrayList<>();
            argsList.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
            argsList.add("-cp");
            argsList.add(JarUtils.getContextPaths(ServletContainer.getDependencyContexts()).get(0));
            argsList.add(ServletContainer.class.getName());
            argsList.addAll(ServletContainer.getServletArgs());

            builder = new ProcessBuilder(argsList);
            builder.environment().put("PATH", PathUtils.getSystemPath());
        }
        
        /**
         * Start the process associated with the example page servlet.
         * @throws IOException if an I/O error occurs
         */
        public void start() throws IOException {
            if (!hasStarted && !isActive()) {
                process = builder.start();
                LOGGER.debug("Activated example page site at: {}", getUrl());
                hasStarted = true;
            }
        }
        
        /**
         * Determine if example page servlet is active.
         * 
         * @return {@code true} if servlet is active; otherwise {@code false}
         */
        public boolean isActive() {
            if (!isActive) {
                isActive = GridUtility.isHostActive(getUrl(), "/grid/admin/ExamplePageServlet");
            }
            return isActive;
        }
        
        /**
         * Stop the example page servlet.
         * 
         * @throws InterruptedException if interrupted while awaiting shutdown
         */
        public void shutdown() throws InterruptedException {
            if (!isActive()) return;
            
            if (process != null) {
                process.destroy();
                int exitCode = process.waitFor();
                if (exitCode == 143 || exitCode == 1) {
                    LOGGER.debug("Terminated example page server process listening to: {}", getUrl());
                    hasStarted = false;
                    isActive = false;
                    return;
                }
            }
            
            if (ServerProcessKiller.killServerProcess(null, getUrl())) {
                hasStarted = false;
                isActive = false;
            }
            
        }
        
        /**
         * Get the example page servlet URL.
         * 
         * @return example page servlet URL
         */
        public URL getUrl() {
            try {
                return URI.create("http://" + HostUtils.getLocalHost() + ":8080").toURL();
            } catch (MalformedURLException e) {
                // nothing to do here
            }
            return null;
        }
    }
    
    /**
     * Get the singleton instance of the example page servlet launcher.
     * 
     * @return {@link Container#INSTANCE} example page servlet launcher
     */
    public static Container getLauncher() {
        return Container.INSTANCE;
    }
}
