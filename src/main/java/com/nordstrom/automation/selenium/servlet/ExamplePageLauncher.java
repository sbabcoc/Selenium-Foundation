package com.nordstrom.automation.selenium.servlet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.examples.ServletContainer;
import com.nordstrom.common.file.PathUtils;
import com.nordstrom.common.jar.JarUtils;

public class ExamplePageLauncher {
    
    public enum Container {
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
            if (!hasStarted) {
                process = builder.start();
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
         * @throws InterruptedException if interrupted while awaiting shutdown
         */
        public void shutdown() throws InterruptedException {
            if (isActive()) {
                process.destroy();
                hasStarted = false;
                isActive = false;
                process.waitFor();
            }
        }
        
        public URL getUrl() {
            try {
                return URI.create("http://" + GridUtility.getLocalHost() + ":8080").toURL();
            } catch (MalformedURLException e) {
                // nothing to do here
            }
            return null;
        }
    }
    
    public static Container getLauncher() {
        return Container.INSTANCE;
    }
}
