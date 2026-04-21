package com.nordstrom.automation.selenium.interfaces;

import java.util.List;
import java.util.Optional;

import com.nordstrom.automation.selenium.grid.HubInstance;
import com.nordstrom.automation.selenium.grid.NodeInstance;

public interface GridInstanceRegistry {

    void registerHub(HubInstance hub);

    void removeHub(HubInstance hub);

    Optional<HubInstance> findHubByPort(int port);

    List<HubInstance> listHubs();

    void registerNode(NodeInstance node);

    void removeNode(NodeInstance node);

    List<NodeInstance> listNodes();

    List<NodeInstance> findNodesByHub(String hubUrl);
}
