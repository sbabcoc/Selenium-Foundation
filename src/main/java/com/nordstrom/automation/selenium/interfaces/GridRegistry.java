package com.nordstrom.automation.selenium.interfaces;

import java.util.List;

import com.nordstrom.automation.selenium.grid.HubInstance;
import com.nordstrom.automation.selenium.grid.NodeInstance;

public interface GridRegistry {

    void saveHub(HubInstance hub);
    void removeHub(HubInstance hub);

    void saveNode(NodeInstance node);
    void removeNode(NodeInstance node);

    List<HubInstance> hubs();
    List<NodeInstance> nodes();
}
