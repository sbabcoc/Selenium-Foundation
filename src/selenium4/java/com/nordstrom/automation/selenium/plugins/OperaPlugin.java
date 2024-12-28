package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class OperaPlugin extends AbstractAppiumPlugin {

    public static final String DRIVER_NAME = "Opera";
    
    public OperaPlugin() {
        super(DRIVER_NAME);
    }
    
    /*
     * Capability                  Default  Description
     * appium:chromedriverPort     9515     The port to start WebDriver process on
     * appium:executable                    The absolute path to a WebDriver binary executable. If set, the driver will use that path instead of its own WebDriver  
     * appium:executableDir                 A directory within which is found any number of WebDriver binaries. If set, the driver will search this directory for
     *                                      WebDrivers of the appropriate version to use for your browser 
     * appium:verbose              false    Set to true to add the --verbose flag when starting WebDriver
     * appium:logPath                       The path to use with the --log-path parameter directing WebDriver to write its log to that path, if set 
     * appium:disableBuildCheck    false    Set to true to add the --disable-build-check flag when starting WebDriver
     * appium:autodownloadEnabled  true     Set to false to disable automatic downloading of Chromedrivers.
     * appium:useSystemExecutable  false    Set to true to use the version of WebDriver bundled with this driver, rather than attempting to download a new one based
     *                                      on the version of the browser under test
     */

    private static final String CAPABILITIES =
            "{\"appium:automationName\":\"Opera\",\"platformName\":\"Windows\",\"browserName\":\"opera\"}";
    
    private static final String BASELINE =
            "{\"appium:automationName\":\"Opera\",\"platformName\":\"Windows\",\"browserName\":\"opera\"," +
             "\"nord:options\":{\"personality\":\"opera\"," +
                               "\"pluginClass\":\"com.nordstrom.automation.selenium.plugins.OperaPlugin\"}}";
    
    private static final Map<String, String> PERSONALITIES;
    
    private static final String DRIVER_CLASS_NAME = "io.appium.java_client.AppiumDriver";
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(DRIVER_NAME, BASELINE);
        PERSONALITIES = Collections.unmodifiableMap(personalities);
    }
    
    @Override
    public String getCapabilities(SeleniumConfig config) {
        return addNordOptions(config, CAPABILITIES);
    }

    @Override
    public Map<String, String> getPersonalities() {
        return PERSONALITIES;
    }
    
    @Override
    public String getDriverClassName() {
        return DRIVER_CLASS_NAME;
    }

}
