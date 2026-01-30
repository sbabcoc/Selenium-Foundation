package com.nordstrom.automation.selenium.utility;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.Capabilities;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.common.file.PathUtils;

public class BinaryFinder {
    
    public interface DriverManager {

        /**
         * Ensures driver for browser exists and returns absolute path
         * to the driver executable.
         */
        Path provisionDriver(String browser);
    }
    
    /**
     * Find/install driver indicated by the specified capabilities.
     * 
     * @param capabilities For driver binaries, the required capabilities for the specified driver
     * @return path to driver supporting specified capabilities as a {@link File} object
     */
    public static Path findDriver(String capabilities) {
        Capabilities caps = SeleniumConfig.getConfig().getCapabilitiesForJson(capabilities)[0];
        DriverManager manager = DriverManagerFactory.create(caps.getBrowserName());
        return manager.provisionDriver(caps.getBrowserName());
    }
    
    /**
    * Find the specified executable file.
    * 
    * @param exeName Name of the executable file to look for in PATH
    * @param exeProperty Name of a system property that specifies the path to the executable file
    * @return The specified executable as a {@link File} object
    * @throws IllegalStateException if the executable is not found or cannot be executed
    */
    public static File findBinary(String exeName, String exeProperty) {
        String defaultPath = PathUtils.findExecutableOnSystemPath(exeName);
        String exePath = System.getProperty(exeProperty, defaultPath);
        checkState(exePath != null,
                "The path to the driver executable must be set by the %s system property",
                exeProperty);

        File exe = new File(exePath);
        checkExecutable(exe);
        return exe;
    }

    /**
     * Ensure that the specified object exists as an executable file.
     * 
     * @param exe executable to check as a {@link File} object
     * @throws IllegalStateException if the executable is not found or cannot be executed
     */
    protected static void checkExecutable(File exe) {
        checkState(exe.exists(), "Specified file does not exist: %s", exe.getAbsolutePath());
        checkState(!exe.isDirectory(), "Specified file is a directory: %s", exe.getAbsolutePath());
        checkState(exe.canExecute(), "Specified file is not executable: %s", exe.getAbsolutePath());
    }

    static final class DriverManagerFactory {

        private DriverManagerFactory() {}

        public static DriverManager create(String browser) {
            int seleniumMajor = SeleniumConfig.getConfig().getVersion();
            if (seleniumMajor >= 4 && !"opera".equalsIgnoreCase(browser)) {
                return new SeleniumManagerImpl();
            }
            return new WebDriverManagerImpl();
        }
    }
    
    static final class SeleniumManagerImpl implements DriverManager {

        private static final String CLASS = "org.openqa.selenium.manager.SeleniumManager";

        @Override
        public Path provisionDriver(String browser) {
            try {
                Class<?> smClass = Class.forName(CLASS);
                Object sm = smClass.getMethod("getInstance").invoke(null);
                Object result = smClass.getMethod("getBinaryPaths", List.class).invoke(sm,
                        Arrays.asList("--browser", browser));
                String driverPath = (String) result.getClass().getMethod("getDriverPath").invoke(result);
                return Paths.get(driverPath);
            } catch (Exception e) {
                throw new RuntimeException("SeleniumManager provisioning failed", e);
            }
        }
    }
    
    static final class WebDriverManagerImpl implements DriverManager {

        private static final String CLASS = "io.github.bonigarcia.wdm.WebDriverManager";

        @Override
        public Path provisionDriver(String browser) {
            try {
                Class<?> wdmClass = Class.forName(CLASS);
                Method getInstance = wdmClass.getMethod("getInstance", String.class);
                Object mgr = getInstance.invoke(null, browser);
                Method setup = mgr.getClass().getMethod("setup");
                setup.invoke(mgr);
                Method getPath = mgr.getClass().getMethod("getDownloadedDriverPath");
                String path = (String) getPath.invoke(mgr);
                return Paths.get(path);
            } catch (Exception e) {
                throw new RuntimeException("WebDriverManager provisioning failed", e);
            }
        }
    }
}
