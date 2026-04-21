package com.nordstrom.automation.selenium.grid;

import java.util.List;

import com.nordstrom.automation.selenium.interfaces.NodeRegistry;

public class NodeManager {

    private final NodeRegistry registry;

    public NodeManager(NodeRegistry registry) {
        this.registry = registry;
    }
    
    public NodeInstance startManagedNode(String hubUrl) {

        Process process = launchNodeProcess(hubUrl);

        Long pid = tryGetPid(process); // optional

        NodeInstance node = new NodeInstance(
                "http://localhost:auto", // or resolved endpoint
                hubUrl,
                pid
        );

        registry.register(node);
        return node;
    }
    
    public void stopManagedNode(NodeInstance node) {

        if (node.pid != null) {
            ProcessHandle.of(node.pid)
                    .ifPresent(ProcessHandle::destroy);
        }

        registry.remove(node);
    }
    
    public List<NodeInstance> listExternalNodes(String hubUrl) {

        // Query Grid API instead of OS introspection
        return queryHubForNodes(hubUrl);
    }
    
}
