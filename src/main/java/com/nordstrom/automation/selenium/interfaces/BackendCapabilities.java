package com.nordstrom.automation.selenium.interfaces;

public interface BackendCapabilities {

    boolean supportsDynamicPorts();

    boolean supportsNodeLifecycleManagement();

    boolean supportsHubLifecycleManagement();

    boolean supportsExternalNodeDiscovery();

    boolean supportsHealthCheck();

    boolean supportsGracefulShutdown();
}
