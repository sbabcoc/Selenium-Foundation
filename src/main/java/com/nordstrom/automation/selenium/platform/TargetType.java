package com.nordstrom.automation.selenium.platform;

import org.openqa.selenium.Capabilities;

import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.SeleniumGrid;
import com.nordstrom.automation.selenium.plugins.EspressoPlugin;
import com.nordstrom.automation.selenium.plugins.Mac2Plugin;
import com.nordstrom.automation.selenium.plugins.RemoteWebDriverPlugin;
import com.nordstrom.automation.selenium.plugins.UiAutomator2Plugin;
import com.nordstrom.automation.selenium.plugins.WindowsPlugin;
import com.nordstrom.automation.selenium.plugins.XCUITestPlugin;

public enum TargetType implements TargetTypeName {
    SUPPORT(SUPPORT_NAME),
    WEB_APP(WEB_APP_NAME, RemoteWebDriverPlugin.class),
    ANDROID(ANDROID_NAME, UiAutomator2Plugin.class, EspressoPlugin.class),
    IOS_APP(IOS_APP_NAME, XCUITestPlugin.class),
    MAC_APP(MAC_APP_NAME, Mac2Plugin.class),
    WINDOWS(WINDOWS_NAME, WindowsPlugin.class);
    
    private String name;
    private Class<?>[] classes;
    
    <T extends DriverPlugin> TargetType(String name, Class<?>... classes) {
        this.name = name;
        this.classes = classes;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
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
                // get fully-populated [Capabilities] object for the specified driver 'personality'
                Capabilities personality = grid.getPersonality(config, (String) caps.getCapability("personality"));
                // extract plug-in class specification from 'personality' object
                Class<?> pluginClass = Class.forName((String) personality.getCapability("pluginClass"));
                // iterate over target plug-in classes
                for (Class<?> thisClass : this.classes) {
                    // if 'personality' is supported by target, run this test
                    if (thisClass.isAssignableFrom(pluginClass)) return true;
                }
            } catch (IllegalArgumentException | ClassNotFoundException e) {
                // nothing to do here
            }
        }
        // don't run
        return false;
    }
}