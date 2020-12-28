package com.nordstrom.automation.selenium.plugins;

import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class ChromePlugin extends RemoteWebDriverPlugin {
    
    public ChromePlugin() {
        super(ChromeCaps.DRIVER_NAME);
    }
    
    /**
     * <b>org.openqa.selenium.chrome.ChromeDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-chrome-driver&lt;/artifactId&gt;
     *  &lt;version&gt;2.53.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
                    "org.openqa.selenium.chrome.ChromeDriver",
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
    public String[] getPropertyNames() {
        return ChromeCaps.getPropertyNames();
    }

}
