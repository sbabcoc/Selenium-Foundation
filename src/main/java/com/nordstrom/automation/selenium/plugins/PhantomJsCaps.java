package com.nordstrom.automation.selenium.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PhantomJsCaps {
    
    private PhantomJsCaps() {
        throw new AssertionError("PhantomJsCaps is a static constants class that cannot be instantiated");
    }

    public static final String DRIVER_NAME = "phantomjs";
    private static final String[] PROPERTY_NAMES = 
    	{ "phantomjs.binary.path", "phantomjs.ghostdriver.path", "phantomjs.logfile.path" };

    private static final String CAPABILITIES =
                    "{\"browserName\":\"phantomjs\",\"maxInstances\":5,\"seleniumProtocol\":\"WebDriver\"}";
    
    private static final String BASELINE = "{\"browserName\":\"phantomjs\"}";
    
    private static final String LOGGING = 
    		"{\"browserName\":\"phantomjs\"," +
    		 "\"loggingPrefs\":{\"browser\":\"WARNING\"}," +
    	     "\"personality\":\"phantomjs.logging\"" +
    		"}";
    
    private static final Map<String, String> PERSONALITIES;
    
    static {
        Map<String, String> personalities = new HashMap<>();
        personalities.put(DRIVER_NAME, BASELINE);
        personalities.put(DRIVER_NAME + ".logging", LOGGING);
        PERSONALITIES = Collections.unmodifiableMap(personalities);
    }
    
    public static String getCapabilities() {
        return CAPABILITIES;
    }

    public static Map<String, String> getPersonalities() {
        return PERSONALITIES;
    }
    
    public static String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

}
