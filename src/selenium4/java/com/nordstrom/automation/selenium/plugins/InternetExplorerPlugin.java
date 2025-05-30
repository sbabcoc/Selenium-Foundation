package com.nordstrom.automation.selenium.plugins;

import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

/**
 * This class is the plug-in for <b>InternetExplorerDriver</b>.
 */
public class InternetExplorerPlugin extends RemoteWebDriverPlugin {
    
    /**
     * Constructor for <b>InternetExplorerPlugin</b> objects.
     */
    public InternetExplorerPlugin() {
        super(InternetExplorerCaps.DRIVER_NAME);
    }
    
    /**
     * <b>org.openqa.selenium.ie.InternetExplorerDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-ie-driver&lt;/artifactId&gt;
     *  &lt;version&gt;4.30.0&lt;/version&gt;
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
        "org.openqa.selenium.ie.InternetExplorerDriver",
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
        return InternetExplorerCaps.getCapabilities();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getPersonalities() {
        return InternetExplorerCaps.getPersonalities();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPropertyNames(String capabilities) {
        return InternetExplorerCaps.getPropertyNames(capabilities);
    }

}
