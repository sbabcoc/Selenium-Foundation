package com.nordstrom.automation.selenium.plugins;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.TypeToken;
import org.openqa.selenium.manager.SeleniumManager;
import org.openqa.selenium.manager.SeleniumManagerOutput.Result;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.exceptions.DriverExecutableNotFoundException;
import com.nordstrom.common.file.OSInfo;
import com.nordstrom.common.file.OSInfo.OSType;

/**
 * This class is the plug-in for <b>OperaDriver</b>.
 */
public class OperaPlugin extends ChromePlugin {
    
    private static final String TEMPLATE = 
            "{\"browserName\":\"chrome\"," +
             "\"goog:chromeOptions\":{\"binary\":\"<binary-path>\"}," +
             "\"nord:options\":{\"personality\":\"opera\"," +
                               "\"driverPath\":\"<driver-path>\"}}";
    
    private static final String BASELINE =
            "{\"browserName\":\"chrome\"," +
             "\"goog:chromeOptions\":{\"args\":[\"--disable-infobars\",\"--disable-dev-shm-usage\"," +
                                               "\"--remote-debugging-port=9222\",\"--no-sandbox\"," +
                                               "\"--disable-gpu\",\"--disable-logging\"]," +
                                     "\"prefs\":{\"credentials_enable_service\":false}}," +
             "\"nord:options\":{\"personality\":\"opera\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.OperaPlugin\"}}";
    
    private static final OSType OS_TYPE;
    private static final Map<String, String> OPERA_TO_CHROMIUM;
    private static final String BINARY_PATH;
    private static final String DRIVER_PATH;
    private static final String CAPABILITIES;
    private static final Map<String, String> PERSONALITIES;
    
    private static final String VERSION_MAPPINGS = "operaChromiumVersions.json";
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    
    static {
        OS_TYPE = OSInfo.getDefault().getType();
        String mappings = JsUtility.getScriptResource(VERSION_MAPPINGS);
        OPERA_TO_CHROMIUM = new Json().toType(mappings, MAP_TYPE);
        
        BINARY_PATH = Objects.requireNonNull(findOperaBinary(), "Failed finding the Opera browser binary");
        DRIVER_PATH = Objects.requireNonNull(findDriverBinary(), "Failed finding the 'chromedriver' binary");
        
        CAPABILITIES = TEMPLATE
                .replace("<binary-path>", BINARY_PATH.replaceAll("\\\\", "/"))
                .replace("<driver-path>", DRIVER_PATH.replaceAll("\\\\", "/"));
        PERSONALITIES = Map.of(OperaCaps.DRIVER_NAME, BASELINE);
    }
    
    /**
     * Constructor for <b>OperaPlugin</b> objects.
     */
    public OperaPlugin() {
        super(OperaCaps.DRIVER_NAME);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getCapabilities(SeleniumConfig config) {
        return CAPABILITIES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getPersonalities() {
        return PERSONALITIES;
    }

    /**
     * Get list of system property names recognized by the driver associated with this plug-in.
     * 
     * @param capabilities JSON {@link org.openqa.selenium.Capabilities Capabilities} object
     * @return list of system property names
     */
    public String[] getPropertyNames(String capabilities) {
        return ChromeCaps.PROPERTY_NAMES;
    }

    /**
     * Get path to installed <b>Opera</b> browser.
     * 
     * @return path to installed <b>Opera</b> browser; {@code null} if browser not found
     */
    public static String findOperaBinary() {
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
    
    /**
     * Detect version of installed <b>Opera</b> browser.
     *  
     * @return version of installed <b>Opera</b> browser; {@code null} if detection fails
     */
    public static String detectOperaVersion() {
        try {
            switch (OS_TYPE) {
            case MACINTOSH:
                return detectMac();
            case UNIX:
                return detectLinux();
            case WINDOWS:
                return detectWindows();
            default:
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String detectWindows() throws Exception {
        // Use PowerShell to get ProductVersion from file metadata
        String[] cmd = { "powershell.exe", "-Command", "\"(Get-Item '" + BINARY_PATH + "').VersionInfo.ProductVersion\"" };
        return runCommand(cmd).trim();
    }

    private static String detectMac() throws Exception {
        String[] cmd = { "/usr/libexec/PlistBuddy", "-c", "Print :CFBundleShortVersionString", BINARY_PATH + "/Contents/Info.plist" };
        return runCommand(cmd).trim();
    }

    private static String detectLinux() throws Exception {
        String[] cmd = { "/bin/sh", "-c", "cat " + BINARY_PATH + "/resources/version" };
        return runCommand(cmd).trim();
    }

    private static String runCommand(String[] command) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            return (line != null) ? line : null;
        }
    }
    
    /**
     * Find 'chromedriver' binary that works with the active installation of Opera.
     * 
     * @return path to 'chromedriver' binary; {@code null} if driver not found
     */
    public static String findDriverBinary() {
        String operaVersion = Objects.requireNonNull(detectOperaVersion(),
                "Failed detecting Opera browser version");
        String operaMajor = operaVersion.split("\\.")[0];
        String chromiumMajor = Objects.requireNonNull(OPERA_TO_CHROMIUM.get(operaMajor),
                "No Chromium version mapping found for Opera major version: " + operaMajor);
        
        try {
            SeleniumManager manager = SeleniumManager.getInstance();
            Result result = manager.getBinaryPaths(List.of(
                    "--driver", "chromedriver",
                    "--driver-version", chromiumMajor));
            return result.getDriverPath();
        } catch (IllegalStateException e) {
            throw new DriverExecutableNotFoundException(ChromeCaps.DRIVER_PATH);
        }
    }
}
