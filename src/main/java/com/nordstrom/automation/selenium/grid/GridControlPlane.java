package com.nordstrom.automation.selenium.grid;

import com.nordstrom.automation.selenium.interfaces.BackendCapabilities;
import com.nordstrom.automation.selenium.interfaces.GridBackend;

public class GridControlPlane implements GridBackend {

    private final GridBackend backend;

    public GridControlPlane(GridBackend backend) {
        this.backend = backend;
    }

    public HubInstance startHub(HubSpec spec) {
        return backend.startHub(spec);
    }

    public NodeInstance startNode(NodeSpec spec) {
        return backend.startNode(spec);
    }
    
    public void requestHubShutdown(HubInstance instance) {
        backend.requestHubShutdown(instance);
    }

    public void requestNodeShutdown(NodeInstance node) {
        backend.requestNodeShutdown(node);
    }

    public BackendCapabilities capabilities() {
        return backend.capabilities();
    }

}
