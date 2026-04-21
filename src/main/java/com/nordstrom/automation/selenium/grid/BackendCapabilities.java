package com.nordstrom.automation.selenium.grid;

public final class BackendCapabilities {

    private final boolean dynamicPorts;
    private final boolean managedNodes;
    private final boolean gracefulShutdown;
    private final boolean healthCheck;
    private final boolean externalDiscovery;

    public BackendCapabilities(boolean dynamicPorts,
                               boolean managedNodes,
                               boolean gracefulShutdown,
                               boolean healthCheck,
                               boolean externalDiscovery) {
        this.dynamicPorts = dynamicPorts;
        this.managedNodes = managedNodes;
        this.gracefulShutdown = gracefulShutdown;
        this.healthCheck = healthCheck;
        this.externalDiscovery = externalDiscovery;
    }

    public boolean supportsDynamicPorts() { return dynamicPorts; }

    public boolean supportsManagedNodes() { return managedNodes; }

    public boolean supportsGracefulShutdown() { return gracefulShutdown; }

    public boolean supportsHealthCheck() { return healthCheck; }

    public boolean supportsExternalDiscovery() { return externalDiscovery; }

    public static BackendCapabilities selenium3() {
        return new BackendCapabilities(
            true,
            true,
            false,
            true,
            true
        );
    }

    public static BackendCapabilities selenium4() {
        return new BackendCapabilities(
            true,
            true,
            true,
            true,
            true
        );
    }
    
    BackendCapabilities dockerCaps = new BackendCapabilities(
            false,
            false,
            true,
            true,
            true
        );
}