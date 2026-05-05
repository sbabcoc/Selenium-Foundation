package com.nordstrom.automation.selenium.interfaces;

import com.nordstrom.automation.selenium.grid.HubInstance;
import com.nordstrom.automation.selenium.grid.HubSpec;
import com.nordstrom.automation.selenium.grid.NodeInstance;
import com.nordstrom.automation.selenium.grid.NodeSpec;

public interface GridBackend {

    HubInstance startHub(HubSpec spec);

    NodeInstance startNode(NodeSpec spec);

    void requestHubShutdown(HubInstance instance);

    void requestNodeShutdown(NodeInstance node);

    BackendCapabilities capabilities();
}
