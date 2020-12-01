package com.nordstrom.automation.selenium.plugins;

import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

/**
 * To expose the massive variations supported by 'appium', the capabilities will be collected in a "settings"
 * object - either the main configuration or a separate configuration specifically for this plug-in.
 * 
 * Significant characteristics:
 * * automationName: Which automation engine to use
 *   - Appium (default)
 *   - UiAutomator2, Espresso, or UiAutomator1 for Android
 *   - XCUITest or Instruments for iOS
 *   - YouiEngine for application built with You.i Engine
 * * platformName: Which mobile OS platform to use
 *    - iOS / Android / FirefoxOS
 * * platformVersion: Mobile OS version
 *    - e.g. :: 7.1, 4.4
 * * deviceName: The kind of mobile device or emulator to use
 *    - iPhone Simulator, iPad Simulator, iPhone Retina 4-inch, Android Emulator, Galaxy S4, etc....
 *      On iOS, this should be one of the valid devices returned by instruments with instruments -s devices.
 *      On Android, this capability is currently ignored, though it remains required.
 * * app: Application to install on launch
 *    - The absolute local path or remote URL of a file, folder, or archive to be installed.
 *       ~ /home/username/file.ipa - IPA application file for iOS device
 *       ~ /home/username/folder.app - APP application folder for iOS Simulator
 *       ~ http://user.name/aut.apk - APK application file for Android
 *       ~ http://user.name/bundle.apks - APKS application archive for Android
 *       ~ C:\\Users\\username\\archive.zip - ZIP file containing one of the preceding
 * * browserName
 * * udid
 * * appPackage
 * * appActivity
 * * wdaLocalPort
 * * derivedDataPath
 * * chromedriverPort
 * * mjpegServerPort
 * * systemPort
 * 
 * Design question: How do we specify automation targets for 'appium' test runs?
 * Unless a specific node is specified, each test will run on every attached node. 
 */
public class AppiumPlugin extends RemoteWebDriverPlugin {

    /**
     * <b>org.openqa.selenium.chrome.ChromeDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.appium&lt;/groupId&gt;
     *  &lt;artifactId&gt;java-client&lt;/artifactId&gt;
     *  &lt;version&gt;7.4.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    /**
     * <b>org.openqa.selenium.chrome.ChromeDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;io.appium&lt;/groupId&gt;
     *  &lt;artifactId&gt;java-client&lt;/artifactId&gt;
     *  &lt;version&gt;3.4.1&lt;/version&gt;
     *  &lt;exclusions&gt;
     *    &lt;exclusion&gt;
     *      &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *      &lt;artifactId&gt;selenium-java&lt;/artifactId&gt;
     *    &lt;/exclusion&gt;
     *  &lt;/exclusions&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
                    "io.appium.java_client.AppiumDriver"}; //,
//                    "org.apache.commons.exec.Executor",
//                    "org.openqa.selenium.remote.RemoteWebDriver",
//                    "com.sun.jna.platform.RasterRangesUtils",
//                    "com.sun.jna.Library"};
    
    @Override
    public String[] getDependencyContexts() {
        return DEPENDENCY_CONTEXTS;
    }

    @Override
    public String getCapabilities(SeleniumConfig config) {
        return AppiumCaps.getCapabilities();
    }

    @Override
    public String getBrowserName() {
        return AppiumCaps.BROWSER_NAME;
    }

    @Override
    public Map<String, String> getPersonalities() {
        return AppiumCaps.getPersonalities();
    }

    @Override
    public String[] getPropertyNames() {
        return AppiumCaps.getPropertyNames();
    }

}
