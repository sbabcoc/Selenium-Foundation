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
     * 
     * <b>org.apache.commons.exec.Executor</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;org.apache.commons&lt;/groupId&gt;
     *   &lt;artifactId&gt;commons-exec&lt;/artifactId&gt;
     *   &lt;version&gt;1.3&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>com.sun.jna.platform.win32.Kernel32</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;net.java.dev.jna&lt;/groupId&gt;
     *   &lt;artifactId&gt;jna-platform&lt;/artifactId&gt;
     *   &lt;version&gt;4.1.0&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>com.sun.jna.win32.StdCallLibrary</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;net.java.dev.jna&lt;/groupId&gt;
     *   &lt;artifactId&gt;jna&lt;/artifactId&gt;
     *   &lt;version&gt;4.1.0&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
                    "org.openqa.selenium.phantomjs.PhantomJSDriver",
                    "org.apache.commons.exec.Executor", "com.sun.jna.platform.win32.Kernel32",
                    "com.sun.jna.win32.StdCallLibrary"};
    
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
