package com.nordstrom.automation.selenium.plugins;

import com.nordstrom.automation.selenium.DriverPlugin;

public class PhantomJsPlugin implements DriverPlugin {
    
    /**
     * <b>org.openqa.selenium.phantomjs.PhantomJSDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;com.codeborne&lt;/groupId&gt;
     *   &lt;artifactId&gt;phantomjsdriver&lt;/artifactId&gt;
     *   &lt;version&gt;1.3.0&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
                    "org.openqa.selenium.phantomjs.PhantomJSDriver",
                    "org.apache.commons.exec.Executor", "com.sun.jna.platform.win32.Kernel32",
                    "com.sun.jna.win32.StdCallLibrary"};
    
    private static final String CAPABILITIES =
                    "{\"browserName\": \"phantomjs\", \"maxInstances\": 5, \"seleniumProtocol\": \"WebDriver\"}";
    
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
    public String getCapabilities() {
        return CAPABILITIES;
    }

}
