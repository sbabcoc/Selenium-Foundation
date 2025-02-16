package com.nordstrom.automation.selenium.plugins;

import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class ChromePlugin extends RemoteWebDriverPlugin {
    
    public ChromePlugin() {
        super(ChromeCaps.DRIVER_NAME);
    }
    
    protected ChromePlugin(String driverName) {
        super(driverName);
    }
    
    /**
     * <b>org.openqa.selenium.chrome.ChromeDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-chrome-driver&lt;/artifactId&gt;
     *  &lt;version&gt;3.141.59&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
        "org.openqa.selenium.chrome.ChromeDriver",
        "org.openqa.selenium.chromium.ChromiumDriver",
        "net.bytebuddy.matcher.ElementMatcher"
    };
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getDependencyContexts() {
        return DEPENDENCY_CONTEXTS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCapabilities(SeleniumConfig config) {
        return ChromeCaps.getCapabilities();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getPersonalities() {
        return ChromeCaps.getPersonalities();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPropertyNames(String capabilities) {
        return ChromeCaps.getPropertyNames(capabilities);
    }

}
