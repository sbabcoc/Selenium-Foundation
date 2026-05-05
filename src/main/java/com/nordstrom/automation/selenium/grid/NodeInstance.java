package com.nordstrom.automation.selenium.grid;

public final class NodeInstance {
    public final String nodeUrl;
    public final String hubUrl;

    // optional metadata (only if YOU launched it)
    public final Long pid;

    public NodeInstance(String nodeUrl, String hubUrl, Long pid) {
        this.nodeUrl = nodeUrl;
        this.hubUrl = hubUrl;
        this.pid = pid;
    }
}
