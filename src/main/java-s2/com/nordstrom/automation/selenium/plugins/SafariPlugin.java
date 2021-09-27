package com.nordstrom.automation.selenium.plugins;

import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class SafariPlugin extends RemoteWebDriverPlugin {
    
    public SafariPlugin() {
        super(SafariCaps.DRIVER_NAME);
    }
    
    /**
     * <b>org.openqa.selenium.safari.SafariDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-safari-driver&lt;/artifactId&gt;
     *  &lt;version&gt;2.53.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
                    "org.openqa.selenium.safari.SafariDriver",
                    "org.apache.commons.exec.Executor",
                    "org.openqa.selenium.remote.RemoteWebDriver",
                    "com.sun.jna.platform.RasterRangesUtils",
                    "com.sun.jna.Library",
                    "org.jboss.netty.channel.ChannelFactory"};
    
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
        return SafariCaps.getCapabilities(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getPersonalities() {
        return SafariCaps.getPersonalities();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPropertyNames() {
        return SafariCaps.getPropertyNames();
    }

}
