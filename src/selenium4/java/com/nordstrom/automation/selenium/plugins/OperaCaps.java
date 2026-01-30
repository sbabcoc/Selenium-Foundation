package com.nordstrom.automation.selenium.plugins;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.TypeToken;
import org.openqa.selenium.manager.SeleniumManager;
import org.openqa.selenium.manager.SeleniumManagerOutput.Result;

import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.exceptions.DriverExecutableNotFoundException;

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

    /** driver name */
    public static final String DRIVER_NAME = "opera";
    
    private static final String TEMPLATE = 
            "{\"browserName\":\"chrome\"," +
             "\"goog:chromeOptions\":{\"binary\":\"<browser-binary>\"}," +
             "\"nord:options\":{\"personality\":\"opera\"," +
                               "\"driverPath\":\"<driver-binary>\"}}";
    
    private static final String BASELINE =
            "{\"browserName\":\"chrome\"," +
             "\"goog:chromeOptions\":{\"args\":[\"--disable-infobars\",\"--disable-dev-shm-usage\"," +
                                               "\"--remote-debugging-port=9222\",\"--no-sandbox\"," +
                                               "\"--disable-gpu\",\"--disable-logging\"]," +
                                     "\"prefs\":{\"credentials_enable_service\":false}}," +
             "\"nord:options\":{\"personality\":\"opera\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.OperaPlugin\"}}";
    
    private static final Map<String, String> OPERA_TO_CHROMIUM;
    private static final String DRIVER_BINARY;
    private static final String CAPABILITIES;
    private static final Map<String, String> PERSONALITIES;
    
    private static final String VERSION_MAPPINGS = "operaChromiumVersions.json";
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    
    static {
        String mappings = JsUtility.getScriptResource(VERSION_MAPPINGS);
        OPERA_TO_CHROMIUM = new Json().toType(mappings, MAP_TYPE);
        
        DRIVER_BINARY = Objects.requireNonNull(findDriverBinary(), "Failed finding the 'chromedriver' binary");
        
        CAPABILITIES = TEMPLATE
                .replace("<browser-binary>", OperaCommon.getOperaBinaryPath().replaceAll("\\\\", "/"))
                .replace("<driver-binary>", DRIVER_BINARY.replaceAll("\\\\", "/"));
        PERSONALITIES = Map.of(DRIVER_NAME, BASELINE);
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
        return ChromeCaps.getPropertyNames(capabilities);
    }
    
    /**
     * Find 'chromedriver' binary that works with the active installation of Opera.
     * 
     * @return path to 'chromedriver' binary; {@code null} if driver not found
     */
    public static String findDriverBinary() {
        String operaVersion = Objects.requireNonNull(OperaCommon.detectOperaVersion(),
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
