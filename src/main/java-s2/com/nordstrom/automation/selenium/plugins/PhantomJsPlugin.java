package com.nordstrom.automation.selenium.plugins;

import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class PhantomJsPlugin extends RemoteWebDriverPlugin {
    
    /**
     * <b>org.openqa.selenium.phantomjs.PhantomJSDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.codeborne&lt;/groupId&gt;
     *  &lt;artifactId&gt;phantomjsdriver&lt;/artifactId&gt;
     *  &lt;version&gt;1.3.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
                    "org.openqa.selenium.phantomjs.PhantomJSDriver",
                    "org.apache.commons.exec.Executor", "com.sun.jna.platform.RasterRangesUtils",
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
        return PhantomJsCaps.getCapabilities();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBrowserName() {
        return PhantomJsCaps.BROWSER_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getPersonalities() {
        return PhantomJsCaps.getPersonalities();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPropertyNames() {
        return PhantomJsCaps.getPropertyNames();
    }

}
