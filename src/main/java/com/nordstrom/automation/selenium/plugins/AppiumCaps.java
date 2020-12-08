package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AppiumCaps {

    private AppiumCaps() {
        throw new AssertionError("AppiumCaps is a static constants class that cannot be instantiated");
    }

    private static final Map<String, String> CAPS_MAP;
    
    static {
    	Map<String, String> capsMap = new HashMap<>();
    	capsMap.put("UiAutomator2",
    			"{\"automationName\":\"UiAutomator2\"," +
                 "\"platformName\":\"Android\"," +
                 "\"browserName\":\"Chrome\"," +
                 "\"maxInstances\":1," +
                 "\"deviceName\":\"Android Emulator\"," +
                 "\"autoGrantPermissions\":true}");
    	capsMap.put("XCUITest",
    			"{\"automationName\":\"XCUITest\"," +
                 "\"platformName\":\"iOS\"," +
                 "\"browserName\":\"Safari\"," +
                 "\"maxInstances\":1," +
                 "\"deviceName\":\"iPhone Simulator\"}");
    	
    	CAPS_MAP = Collections.unmodifiableMap(capsMap);
    }

    public static final String[] DRIVER_NAMES =
            { "UiAutomator2", "XCUITest", "Espresso", "YouiEngine", "Futter", "Windows", "Mac", "Mac2" };

    private static final String[] PROPERTY_NAMES = {};

    private static final Map<String, String> PERSONALITIES;

    static {
        Map<String, String> personalities = new HashMap<>();
        PERSONALITIES = Collections.unmodifiableMap(personalities);
    }

    public static String getCapabilities(String automationName) {
        return CAPS_MAP.get(automationName);
    }

    public static Map<String, String> getPersonalities() {
        return PERSONALITIES;
    }

    public static String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

}
