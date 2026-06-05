package com.nordstrom.automation.selenium.platform;

import java.util.Map;

import org.openqa.selenium.Capabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.core.SeleniumGrid;

/**
 * This enumeration defines a platform that associates test methods with target characteristics and supporting driver
 * plug-in types.
 */
public enum TargetType implements TargetTypeName {
    /**
     * target: support feature<br>
     * driver: (not-applicable)
     */
    SUPPORT(SUPPORT_NAME),

    /**
     * target: web application<br>
     * driver: {@code RemoteWebDriverPlugin}, {@code UiAutomator2Plugin}, {@code XCUITestPlugin}
     */
    WEB_APP(WEB_APP_NAME,
        "com.nordstrom.automation.selenium.plugins.RemoteWebDriverPlugin",
        "com.nordstrom.automation.selenium.plugins.UiAutomator2Plugin",
        "com.nordstrom.automation.selenium.plugins.XCUITestPlugin"),

    /**
     * target: Android application<br>
     * driver: {@code UiAutomator2Plugin}, {@code EspressoPlugin}
     */
    ANDROID(ANDROID_NAME,
        "com.nordstrom.automation.selenium.plugins.UiAutomator2Plugin",
        "com.nordstrom.automation.selenium.plugins.EspressoPlugin"),

    /**
     * target: iOS application<br>
     * driver: {@code XCUITestPlugin}
     */
    IOS_APP(IOS_APP_NAME,
        "com.nordstrom.automation.selenium.plugins.XCUITestPlugin"),

    /**
     * target: Macintosh application<br>
     * driver: {@code Mac2Plugin}
     */
    MAC_APP(MAC_APP_NAME,
        "com.nordstrom.automation.selenium.plugins.Mac2Plugin"),

    /**
     * target: Windows application<br>
     * driver: {@code WindowsPlugin}
     */
    WINDOWS(WINDOWS_NAME,
        "com.nordstrom.automation.selenium.plugins.WindowsPlugin");

    private String name;
    private String[] classNames;

    <T extends DriverPlugin> TargetType(String name, String... classNames) {
        this.name = name;
        this.classNames = classNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(String contextPlatform) {
        // always run 'support' tests
        if (this == SUPPORT) return true;
        // if this matches context platform
        if (name.equals(contextPlatform)) {
            // get Selenium configuration object
            SeleniumConfig config = SeleniumConfig.getConfig();
            // get default desired capabilities
            Capabilities caps = config.getCurrentCapabilities();
            // get active Selenium Grid object
            SeleniumGrid grid = config.getSeleniumGrid();
            try {
                // get map of custom options from capabilities
                Map<String, Object> options = GridUtility.getNordOptions(caps);
                // get fully-populated [Capabilities] object for the specified driver 'personality'
                Capabilities personality = grid.getPersonality(config, (String) options.get("personality"));
                // get map of custom options from personality
                options = GridUtility.getNordOptions(personality);
                // extract plug-in class specification from 'personality' object
                Class<?> pluginClass = Class.forName((String) options.get("pluginClass"));
                // iterate over target plug-in class names
                for (String className : this.classNames) {
                    try {
                        Class<?> thisClass = Class.forName(className);
                        // if 'personality' is supported by target, run this test
                        if (thisClass.isAssignableFrom(pluginClass)) return true;
                    } catch (ClassNotFoundException ignored) {
                        // plugin not on classpath - skip
                    }
                }
            } catch (NullPointerException | IllegalArgumentException | ClassNotFoundException eaten) {
                Logger logger = LoggerFactory.getLogger(TargetType.class);
                logger.warn("Target platform check triggered exception", eaten);
            }
        }
        // don't run
        return false;
    }
}
