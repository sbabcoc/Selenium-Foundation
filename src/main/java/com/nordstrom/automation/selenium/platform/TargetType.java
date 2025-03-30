package com.nordstrom.automation.selenium.platform;

import java.util.Map;

import org.openqa.selenium.Capabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.core.SeleniumGrid;
import com.nordstrom.automation.selenium.plugins.EspressoPlugin;
import com.nordstrom.automation.selenium.plugins.Mac2Plugin;
import com.nordstrom.automation.selenium.plugins.RemoteWebDriverPlugin;
import com.nordstrom.automation.selenium.plugins.UiAutomator2Plugin;
import com.nordstrom.automation.selenium.plugins.WindowsPlugin;
import com.nordstrom.automation.selenium.plugins.XCUITestPlugin;

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
     * driver: {@link RemoteWebDriverPlugin}, {@link UiAutomator2Plugin}
     */
    WEB_APP(WEB_APP_NAME, RemoteWebDriverPlugin.class, UiAutomator2Plugin.class),
    
    /**
     * target: Android application<br>
     * driver: {@link UiAutomator2Plugin}, {@link EspressoPlugin}
     */
    ANDROID(ANDROID_NAME, UiAutomator2Plugin.class, EspressoPlugin.class),
    
    /**
     * target: iOS application<br>
     * driver: {@link XCUITestPlugin}
     */
    IOS_APP(IOS_APP_NAME, XCUITestPlugin.class),
    
    /**
     * target: Macintosh application<br>
     * driver: {@link Mac2Plugin}
     */
    MAC_APP(MAC_APP_NAME, Mac2Plugin.class),
    
    /**
     * target: Windows application<br>
     * driver: {@link WindowsPlugin}
     */
    WINDOWS(WINDOWS_NAME, WindowsPlugin.class);
    
    private String name;
    private Class<?>[] classes;
    
    <T extends DriverPlugin> TargetType(String name, Class<?>... classes) {
        this.name = name;
        this.classes = classes;
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
                // iterate over target plug-in classes
                for (Class<?> thisClass : this.classes) {
                    // if 'personality' is supported by target, run this test
                    if (thisClass.isAssignableFrom(pluginClass)) return true;
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
