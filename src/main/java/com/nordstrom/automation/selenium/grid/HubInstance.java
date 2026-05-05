package com.nordstrom.automation.selenium.grid;

public final class HubInstance {
    public final int hubPort;
    public final Integer eventBusPubPort;
    public final Integer eventBusSubPort;

    // optional (may be null in Termux/proot)
    public final Long pid;

    public final String baseUrl;

    public HubInstance(int hubPort,
                        Integer pub,
                        Integer sub,
                        Long pid,
                        String baseUrl) {
        this.hubPort = hubPort;
        this.eventBusPubPort = pub;
        this.eventBusSubPort = sub;
        this.pid = pid;
        this.baseUrl = baseUrl;
    }
}
