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
     *  &lt;version&gt;3.141.59&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
                    "org.openqa.selenium.safari.SafariDriver",
                    "org.apache.commons.exec.Executor"};
    
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
    public String getCapabilitiesForDriver(SeleniumConfig config, String driverName) {
        requireDriverName(driverName);
        return SafariCaps.getCapabilities();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDriverName() {
        return SafariCaps.DRIVER_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getPersonalitiesForDriver(String driverName) {
        requireDriverName(driverName);
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
