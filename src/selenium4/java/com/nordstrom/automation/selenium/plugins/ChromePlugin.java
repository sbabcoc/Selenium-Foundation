package com.nordstrom.automation.selenium.plugins;

import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;

/**
 * This class is the plug-in for <b>ChromeDriver</b>.
 */
public class ChromePlugin extends RemoteWebDriverPlugin {
    
    /**
     * Constructor for <b>ChromePlugin</b> objects.
     */
    public ChromePlugin() {
        super(ChromeCaps.DRIVER_NAME);
    }
    
    /**
     * Extension constructor for <b>ChromeDriver</b> subclass objects.
     * 
     * @param browserName browser name
     */
    protected ChromePlugin(String browserName) {
        super(browserName);
    }
    
    /**
     * <b>org.openqa.selenium.chrome.ChromeDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-chrome-driver&lt;/artifactId&gt;
     *  &lt;version&gt;4.30.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     *
     * <b>org.openqa.selenium.chrome.ChromiumDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-chromium-driver&lt;/artifactId&gt;
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
