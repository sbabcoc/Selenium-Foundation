package com.nordstrom.automation.selenium.plugins;

import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class FirefoxPlugin extends RemoteWebDriverPlugin {
    
    public FirefoxPlugin() {
        super(FirefoxCaps.DRIVER_NAME);
    }
    
    /**
     * <b>org.openqa.selenium.firefox.FirefoxDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-firefox-driver&lt;/artifactId&gt;
     *  &lt;version&gt;3.141.59&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
        "org.openqa.selenium.firefox.FirefoxDriver",
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
        return FirefoxCaps.getCapabilities();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getPersonalities() {
        return FirefoxCaps.getPersonalities();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPropertyNames(String capabilities) {
        return FirefoxCaps.getPropertyNames(capabilities);
    }

}
