package com.nordstrom.automation.selenium.plugins;

import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

/**
 * This class is the plug-in for <b>GhostDriver</b>.
 */
public class PhantomJsPlugin extends RemoteWebDriverPlugin {
    
    /**
     * Constructor for <b>GhostPlugin</b> objects.
     */
    public PhantomJsPlugin() {
        super(PhantomJsCaps.DRIVER_NAME);
    }
    
    /**
     * <b>org.openqa.selenium.phantomjs.PhantomJSDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.codeborne&lt;/groupId&gt;
     *  &lt;artifactId&gt;phantomjsdriver&lt;/artifactId&gt;
     *  &lt;version&gt;1.4.4&lt;/version&gt;
     *  &lt;exclusions&gt;
     *    &lt;exclusion&gt;
     *      &lt;groupId&gt;*&lt;/groupId&gt;
     *      &lt;artifactId&gt;*&lt;/artifactId&gt;
     *    &lt;/exclusion&gt;
     *  &lt;/exclusions&gt;
     *&lt;/dependency&gt;</pre>
     *
     * <b>net.bytebuddy.matcher.ElementMatcher</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;net.bytebuddy&lt;/groupId&gt;
     *  &lt;artifactId&gt;byte-buddy&lt;/artifactId&gt;
     *  &lt;version&gt;1.17.5&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
        "org.openqa.selenium.phantomjs.PhantomJSDriver",
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
        return PhantomJsCaps.getCapabilities();
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
    public String[] getPropertyNames(String capabilities) {
        return PhantomJsCaps.getPropertyNames(capabilities);
    }

}
