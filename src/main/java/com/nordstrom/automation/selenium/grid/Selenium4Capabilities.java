package com.nordstrom.automation.selenium.grid;

import com.nordstrom.automation.selenium.interfaces.BackendCapabilities;

public class Selenium4Capabilities implements BackendCapabilities {

    public boolean supportsDynamicPorts() { return true; }

    public boolean supportsNodeLifecycleManagement() { return true; }

    public boolean supportsHubLifecycleManagement() { return true; }

    public boolean supportsExternalNodeDiscovery() { return true; }

    public boolean supportsHealthCheck() { return true; }

    public boolean supportsGracefulShutdown() { return true; }
}
