package com.nordstrom.automation.selenium.platform;

import com.nordstrom.automation.selenium.plugins.EspressoPlugin;
import com.nordstrom.automation.selenium.plugins.Mac2Plugin;
import com.nordstrom.automation.selenium.plugins.RemoteWebDriverPlugin;
import com.nordstrom.automation.selenium.plugins.UiAutomator2Plugin;
import com.nordstrom.automation.selenium.plugins.WindowsPlugin;
import com.nordstrom.automation.selenium.plugins.XCUITestPlugin;

/**
 * This interface defines the names of the platforms supported by the <b>Selenium Foundation</b> unit tests.
 */
public interface TargetTypeName extends PlatformEnum {
    /**
     * target: support feature<br>
     * driver: (not-applicable)
     */
    String SUPPORT_NAME = "support";
    
    /**
     * target: web application<br>
     * driver: {@link RemoteWebDriverPlugin}
     */
    String WEB_APP_NAME = "web-app";
    
    /**
     * target: Android application<br>
     * driver: {@link UiAutomator2Plugin}, {@link EspressoPlugin}
     */
    String ANDROID_NAME = "android";
    
    /**
     * target: iOS application<br>
     * driver: {@link XCUITestPlugin}
     */
    String IOS_APP_NAME = "ios-app";
    
    /**
     * target: Macintosh application<br>
     * driver: {@link Mac2Plugin}
     */
    String MAC_APP_NAME = "mac-app";
    
    /**
     * target: Windows application<br>
     * driver: {@link WindowsPlugin}
     */
    String WINDOWS_NAME = "windows";
}
