package com.nordstrom.automation.selenium.plugins;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class OperaCaps {
    
    private OperaCaps() {
        throw new AssertionError("OperaCaps is a static constants class that cannot be instantiated");
    }

    public static final String DRIVER_NAME = "operablink";
    public static final String OPERA_BINARY = "selenium.opera.binary";
    public static final String OPTIONS_KEY = "operaOptions";
    public static final String BINARY_KEY = "binary";
    private static final String[] PROPERTY_NAMES = { "webdriver.opera.driver" };
    
    private static final String CAPABILITIES = "{\"browserName\":\"operablink\",\"maxInstances\":5,\"seleniumProtocol\":\"WebDriver\"}";
    
    private static final String BASELINE = "{\"browserName\":\"operablink\"}";
    
    private static final Map<String, String> PERSONALITIES;
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(DRIVER_NAME, BASELINE);
        PERSONALITIES = Collections.unmodifiableMap(personalities);
    }
    
    @SuppressWarnings("unchecked")
    public static String getCapabilities(SeleniumConfig config) {
        String binaryPath = config.getString(OPERA_BINARY);
        Objects.requireNonNull(binaryPath, "Path to Opera binary must be specified in setting [" + OPERA_BINARY + "]");
        
        DesiredCapabilities caps = new DesiredCapabilities().merge(config.getCapabilitiesForJson(CAPABILITIES)[0]);
        Map<String, Object> options = (Map<String, Object>) caps.getCapability(OPTIONS_KEY);
        if (options == null) {
            options = new HashMap<>();
        }
        options.put(BINARY_KEY, new File(binaryPath).getPath());
        caps.setCapability(OPTIONS_KEY, options);
        
        return config.toJson(caps);
    }

    public static Map<String, String> getPersonalities() {
        return PERSONALITIES;
    }

    public static String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

}
