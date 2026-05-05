package com.nordstrom.automation.selenium.grid;

import com.nordstrom.automation.selenium.interfaces.BackendCapabilities;
import com.nordstrom.automation.selenium.interfaces.GridBackend;

public class Selenium3Backend implements GridBackend {

    @Override
    public HubInstance startHub(HubSpec spec) {
        Process hub = new ProcessBuilder(
                "selenium-server-standalone",
                "-role", "hub",
                "-port", String.valueOf(spec.preferredPort)
        ).start();

        return new HubInstance(spec.preferredPort, null, null, tryPid(hub), "http://localhost:" + spec.preferredPort);
    }

    @Override
    public NodeInstance startNode(NodeSpec spec) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void requestHubShutdown(HubInstance instance) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void requestNodeShutdown(NodeInstance node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public BackendCapabilities capabilities() {
        // TODO Auto-generated method stub
        return null;
    }
    
    private static Long tryPid(Process process) {
        if (process == null) return null;
        try {
            long pid = process.pid();
            return (pid > 0) ? pid : null;
        } catch (UnsupportedOperationException e) {
            return null;
        }
    }
    
    private static Long tryPid2(Process process) {
        try {
            // UNIXProcess (Linux/macOS)
            if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
                java.lang.reflect.Field f = process.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                return (long) f.get(process);
            }

            // WindowsProcess (Windows)
            if (process.getClass().getName().equals("java.lang.Win32Process") ||
                process.getClass().getName().equals("java.lang.ProcessImpl")) {

                java.lang.reflect.Field f = process.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                long handle = (long) f.get(process);

                // Convert handle → PID via JNA or native call (not trivial)
                // Without JNA, you can’t reliably finish this part
            }

        } catch (Exception ignored) {}

        return null;
    }
}
