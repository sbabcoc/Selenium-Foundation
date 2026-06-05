package com.nordstrom.automation.selenium.platform;

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
     * driver: {@code RemoteWebDriverPlugin}
     */
    String WEB_APP_NAME = "web-app";

    /**
     * target: Android application<br>
     * driver: {@code UiAutomator2Plugin}, {@code EspressoPlugin}
     */
    String ANDROID_NAME = "android";

    /**
     * target: iOS application<br>
     * driver: {@code XCUITestPlugin}
     */
    String IOS_APP_NAME = "ios-app";

    /**
     * target: Macintosh application<br>
     * driver: {@code Mac2Plugin}
     */
    String MAC_APP_NAME = "mac-app";

    /**
     * target: Windows application<br>
     * driver: {@code WindowsPlugin}
     */
    String WINDOWS_NAME = "windows";
}
