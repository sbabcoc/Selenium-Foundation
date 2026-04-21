package com.nordstrom.automation.selenium.interfaces;

import java.util.List;

import com.nordstrom.automation.selenium.grid.NodeInstance;

public interface NodeRegistry {
    void register(NodeInstance node);
    void remove(NodeInstance node);
    List<NodeInstance> list();
    List<NodeInstance> findByHub(String hubUrl);
}
