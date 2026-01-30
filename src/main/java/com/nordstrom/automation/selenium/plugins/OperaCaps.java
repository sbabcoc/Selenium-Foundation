package com.nordstrom.automation.selenium.plugins;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.nordstrom.automation.selenium.exceptions.DriverExecutableNotFoundException;
import com.nordstrom.automation.selenium.utility.BinaryFinder;
import com.nordstrom.common.file.OSInfo;
import com.nordstrom.common.file.OSInfo.OSType;

/**
 * This class defines properties and methods used by plug-ins that support the Opera browser.
 */
public class OperaCaps {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private OperaCaps() {
        throw new AssertionError("OperaCaps is a static constants class that cannot be instantiated");
    }

    private static final OSType OS_TYPE;
    private static final String BROWSER_BINARY;
    
    /** driver name */
    public static final String DRIVER_NAME = "opera";
    /** driver path system property */
    public static final String DRIVER_PATH = "webdriver.opera.driver";
    /** log file path system property */
    public static final String LOGFILE_PATH = "webdriver.opera.logfile";
    /** verbose logging system property */
    public static final String VERBOSE_LOG = "webdriver.opera.verboseLogging";
    /** "silent mode" system property */
    public static final String SILENT_MODE = "webdriver.opera.silentOutput";
    /** extension capability name for <b>ChromeOptions</b> */
    public static final String OPTIONS_KEY = "goog:chromeOptions";
    
    private static final String[] PROPERTY_NAMES = 
        { DRIVER_PATH, LOGFILE_PATH, VERBOSE_LOG, SILENT_MODE };
    
    private static final String CAPABILITIES = 
            "{\"browserName\":\"opera\"}";
    
    private static final String BASELINE_TEMPLATE = 
            "{\"browserName\":\"opera\"," +
             "\"goog:chromeOptions\":{\"binary\":\"<browser-binary>\",\"w3c\":false}," +
             "\"nord:options\":{\"personality\":\"opera\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.OperaPlugin\"}}";
    
    private static final String BASELINE;
    private static final Map<String, String> PERSONALITIES;
    
    static {
        OS_TYPE = OSInfo.getDefault().getType();
        BROWSER_BINARY = Objects.requireNonNull(findOperaBinary(), "Failed finding the Opera browser binary");
        BASELINE = BASELINE_TEMPLATE
                .replace("<browser-binary>", getOperaBinaryPath().replaceAll("\\\\", "/"));
        
        Map<String, String> personalities = new HashMap<>();
        personalities.put(DRIVER_NAME, BASELINE);
        PERSONALITIES = Collections.unmodifiableMap(personalities);
    }
    
    /**
     * Get capabilities supported by this plug-in.
     * 
     * @return core {@link org.openqa.selenium.Capabilities Capabilities} as JSON object
     */
    public static String getCapabilities() {
        return CAPABILITIES;
    }

    /**
     * Get browser "personalities" provided by this plug-in.
     * 
     * @return map of JSON {@link org.openqa.selenium.Capabilities Capabilities} objects keyed by "personality" name
     */
    public static Map<String, String> getPersonalities() {
        return PERSONALITIES;
    }

    /**
     * Get list of system property names recognized by the driver associated with this plug-in.
     * <p>
     * <b>NOTE</b>: As a side-effect, this method invokes <b>WebDriverManager</b> (Selenium 3) or
     * <b>SeleniumManager</b> (Selenium 4) to acquire the driver path. If a driver that supports the
     * browser corresponding to the specified capabilities is not found, the manager will attempt to
     * install one. If the manager acquires the driver path, this method stores it in the associated
     * system property.
     * 
     * @param capabilities JSON {@link org.openqa.selenium.Capabilities Capabilities} object
     * @return list of system property names
     */
    public static String[] getPropertyNames(String capabilities) {
        try {
            Path driverPath = BinaryFinder.findDriver(capabilities);
            System.setProperty(DRIVER_PATH, driverPath.toAbsolutePath().toString());
        } catch (IllegalStateException e) {
            throw new DriverExecutableNotFoundException(DRIVER_PATH);
        }
        return PROPERTY_NAMES;
    }
    
    /**
     * Get path to installed <b>Opera</b> browser.
     * 
     * @return path to installed <b>Opera</b> browser; {@code null} if browser not found
     */
    public static String getOperaBinaryPath() {
        return BROWSER_BINARY;
    }
    
    /**
     * Get path to installed <b>Opera</b> browser.
     * 
     * @return path to installed <b>Opera</b> browser; {@code null} if browser not found
     */
    private static String findOperaBinary() {
        switch (OS_TYPE) {
        case MACINTOSH:
            return findOnMac();
        case UNIX:
            return findOnLinux();
        case WINDOWS:
            return findOnWindows();
        default:
            return null;
        }
    }

    private static String findOnWindows() {
        String[] possiblePaths = { "C:\\Program Files\\Opera\\opera.exe",
                System.getenv("LOCALAPPDATA") + "\\Programs\\Opera\\opera.exe" };

        return firstExistingPath(possiblePaths);
    }

    private static String findOnMac() {
        String[] possiblePaths = { "/Applications/Opera.app/Contents/MacOS/Opera",
                System.getProperty("user.home") + "/Applications/Opera.app/Contents/MacOS/Opera" };

        return firstExistingPath(possiblePaths);
    }

    private static String findOnLinux() {
        String[] possiblePaths = { "/usr/bin/opera", "/usr/local/bin/opera", "/opt/opera/opera" };

        return firstExistingPath(possiblePaths);
    }

    private static String firstExistingPath(String[] paths) {
        for (String path : paths) {
            if (path != null && Files.exists(Paths.get(path))) {
                return path;
            }
        }
        return null;
    }
}
