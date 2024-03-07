package com.nordstrom.automation.selenium.servlet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.os.CommandLine;

import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.examples.ServletContainer;
import com.nordstrom.common.file.PathUtils;
import com.nordstrom.common.jar.JarUtils;

@SuppressWarnings("deprecation")
public class ExamplePageLauncher {
    
    public enum Container {
        INSTANCE;
        
        private final CommandLine process;
        private boolean hasStarted = false;
        private boolean isActive = false;
        
        private Container() {
            List<String> argsList = new ArrayList<>();
            argsList.add("-cp");
            argsList.add(JarUtils.getContextPaths(ServletContainer.getDependencyContexts()).get(0));
            argsList.add(ServletContainer.class.getName());

            String executable = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            process = new CommandLine(executable, argsList.toArray(new String[0]));
            process.setEnvironmentVariable("PATH", PathUtils.getSystemPath());
        }
        
        /**
         * Start the process associated with the example page servlet.
         */
        public void start() {
            if (!hasStarted) {
                process.executeAsync();
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
         */
        public void shutdown() {
            if (isActive()) {
                process.destroy();
                hasStarted = false;
                isActive = false;
            }
        }
        
        public URL getUrl() {
            try {
                return new URL("http://" + GridUtility.getLocalHost() + ":8080");
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
