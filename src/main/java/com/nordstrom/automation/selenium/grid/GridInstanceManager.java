package com.nordstrom.automation.selenium.grid;

import java.io.IOException;
import java.util.List;

import com.nordstrom.automation.selenium.interfaces.GridInstanceRegistry;
import com.nordstrom.automation.selenium.utility.GridHubPortAllocator;
import com.nordstrom.automation.selenium.utility.GridHubPortAllocator.GridPorts;

public class GridInstanceManager {

    private final GridInstanceRegistry registry;

    public GridInstanceManager(GridInstanceRegistry registry) {
        this.registry = registry;
    }

    public GridInstance start(int hubPort) throws IOException {

        GridPorts ports = GridHubPortAllocator.allocate(hubPort);

        Process process = launchGrid(ports);

        Long pid = getPidIfAvailable(process);

        GridInstance instance = new GridInstance(
                ports.hubPort,
                ports.eventBusPubPort,
                ports.eventBusSubPort,
                pid,
                "http://127.0.0.1:" + ports.hubPort
        );

        registry.register(instance);

        return instance;
    }
    
    public void stop(GridInstance instance) {

        // Preferred path: PID shutdown
        if (instance.pid != null) {
            try {
                ProcessHandle.of(instance.pid)
                        .ifPresent(ProcessHandle::destroy);
                registry.remove(instance);
                return;
            } catch (Exception ignored) {}
        }

        // Fallback path: port-based shutdown strategy
        stopByPortStrategy(instance);
        registry.remove(instance);
    }
    
    private void stopByPortStrategy(GridInstance instance) {

        // Option A: if you control startup → send shutdown signal
        tryShutdownEndpoint(instance.baseUrl);

        // Option B: last resort (safe, not hacky)
        // do nothing except mark as stopped
    }
    
    public GridInstance startHub(int hubPort) {
        return hubManager.start(hubPort);
    }

    public void stopHub(GridInstance instance) {
        hubManager.stop(instance);
    }
    
    public NodeInstance startNode(String hubUrl) {
        return nodeManager.startManagedNode(hubUrl);
    }

    public void stopNode(NodeInstance node) {
        nodeManager.stopManagedNode(node);
    }
    
    public List<NodeInstance> getExternalNodes(String hubUrl) {
        return nodeManager.listExternalNodes(hubUrl);
    }
    
    