package com.nordstrom.automation.selenium.plugins;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import com.nordstrom.common.file.OSInfo;
import com.nordstrom.common.file.OSInfo.OSType;

class OperaCommon {

    /**
     * Private constructor to prevent instantiation.
     */
    private OperaCommon() {
        throw new AssertionError("OperaCommon is a static constants class that cannot be instantiated");
    }

    private static final OSType OS_TYPE;
    private static final String BROWSER_BINARY;
    
    static {
        OS_TYPE = OSInfo.getDefault().getType();
        BROWSER_BINARY = Objects.requireNonNull(findOperaBinary(), "Failed finding the Opera browser binary");
    }
    
    /**
     * Get path to installed <b>Opera</b> browser.
     * 
     * @return path to installed <b>Opera</b> browser; {@code null} if browser not found
     */
    public static String getOperaBinaryPath() {
        return BROWSER_BINARY;
    }
    
    /**
     * Get path to installed <b>Opera</b> browser.
     * 
     * @return path to installed <b>Opera</b> browser; {@code null} if browser not found
     */
    private static String findOperaBinary() {
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
        String[] cmd = { "powershell.exe", "-Command", "\"(Get-Item '" + BROWSER_BINARY + "').VersionInfo.ProductVersion\"" };
        return runCommand(cmd).trim();
    }

    private static String detectMac() throws Exception {
        String operaAppPath = BROWSER_BINARY.substring(0, BROWSER_BINARY.indexOf(".app") + 4);
        String[] cmd = { "/usr/libexec/PlistBuddy", "-c", "Print :CFBundleShortVersionString", operaAppPath + "/Contents/Info.plist" };
        return runCommand(cmd).trim();
    }

    private static String detectLinux() throws Exception {
        String[] cmd = { "/bin/sh", "-c", "cat " + BROWSER_BINARY + "/resources/version" };
        return runCommand(cmd).trim();
    }

    private static String runCommand(String[] command) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            return (line != null) ? line : null;
        }
    }
}
