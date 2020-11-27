package com.nordstrom.automation.selenium.plugins;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.LocalSeleniumGrid.LocalGridServer;
import com.nordstrom.automation.selenium.core.SeleniumGrid.GridServer;

import net.bytebuddy.implementation.Implementation;

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
public class AppiumPlugin implements DriverPlugin {

    @Override
    public String[] getDependencyContexts() {
        return new String[0];
    }

    @Override
    public String getCapabilities(SeleniumConfig config) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBrowserName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getPersonalities() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getPropertyNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LocalGridServer start(SeleniumConfig config, String launcherClassName, String[] dependencyContexts,
            GridServer hubServer, DriverPlugin driverPlugin, Path workingPath, Path outputPath) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Implementation getWebElementCtor(WebDriver driver, Class<? extends WebElement> refClass) {
        // TODO Auto-generated method stub
        return null;
    }

}
