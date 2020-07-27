package com.nordstrom.automation.selenium.plugins;

import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class FirefoxPlugin extends RemoteWebDriverPlugin {
    
    /**
     * For Selenium 2.53.1, use Firefox 47.0.1
     * <p>
     * <b>org.openqa.selenium.firefox.FirefoxDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-firefox-driver&lt;/artifactId&gt;
     *  &lt;version&gt;2.53.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
                    "org.openqa.selenium.firefox.FirefoxDriver",
                    "org.apache.commons.exec.Executor",
                    "org.openqa.selenium.remote.RemoteWebDriver",
                    "com.sun.jna.platform.RasterRangesUtils",
                    "com.sun.jna.Library"};
    
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
    public String getBrowserName() {
        return FirefoxCaps.BROWSER_NAME;
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
    public String[] getPropertyNames() {
        return FirefoxCaps.getPropertyNames();
    }

}
