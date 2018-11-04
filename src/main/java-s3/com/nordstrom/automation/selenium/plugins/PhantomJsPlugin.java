package com.nordstrom.automation.selenium.plugins;

import com.nordstrom.automation.selenium.DriverPlugin;

public class PhantomJsPlugin implements DriverPlugin {
    
    /**
     * <b>org.openqa.selenium.phantomjs.PhantomJSDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;com.codeborne&lt;/groupId&gt;
     *   &lt;artifactId&gt;phantomjsdriver&lt;/artifactId&gt;
     *   &lt;version&gt;1.4.4.0&lt;/version&gt;
     *   &lt;exclusions&gt;
     *     &lt;exclusion&gt;
     *       &lt;groupId&gt;*&lt;/groupId&gt;
     *       &lt;artifactId&gt;*&lt;/artifactId&gt;
     *     &lt;/exclusion&gt;
     *   &lt;/exclusions&gt;
     * &lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
                    "org.openqa.selenium.phantomjs.PhantomJSDriver",
                    "org.apache.commons.exec.Executor",
                    "net.bytebuddy.matcher.ElementMatcher"};
    
    private static final String BROWSER_NAME = "phantomjs";

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getDependencyContexts() {
        return DEPENDENCY_CONTEXTS;
    }

    @Override
    public String getBrowserName() {
        return BROWSER_NAME;
    }
}
