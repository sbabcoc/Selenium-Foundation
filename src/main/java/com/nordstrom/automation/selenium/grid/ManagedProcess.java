package com.nordstrom.automation.selenium.grid;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class ManagedProcess {

    private final ProcessHandle handle;   // may be null
    private final Long pid;               // persisted fallback
    private final String endpoint;        // e.g. http://localhost:4444

    public ManagedProcess(Process process, String endpoint) {
        this.handle = process.toHandle();
        this.pid = handle.pid();
        this.endpoint = endpoint;
    }

    public ManagedProcess(Long pid, String endpoint) {
        this.handle = null;
        this.pid = pid;
        this.endpoint = endpoint;
    }

    public boolean stop(BackendCapabilities caps) {

        // 1. Try graceful shutdown first if supported
        if (caps.supportsGracefulShutdown() && endpoint != null) {
            if (tryHttpShutdown(endpoint)) {
                return true;
            }
        }

        // 2. Try live ProcessHandle (best case)
        if (handle != null && handle.isAlive()) {
            handle.destroy();
            return true;
        }

        // 3. Try PID fallback (after restart)
        if (pid != null) {
            return ProcessHandle.of(pid)
                    .map(ph -> {
                        ph.destroy();
                        return true;
                    })
                    .orElse(false);
        }

        // 4. Last resort: give up safely
        return false;
    }

    public boolean stopAndWait(BackendCapabilities caps, Duration timeout) {
        if (!stop(caps)) return false;

        if (pid != null) {
            return ProcessHandle.of(pid)
                    .map(ph -> ph.onExit()
                        .completeOnTimeout(null, timeout.toMillis(), TimeUnit.MILLISECONDS)
                        .join() != null)
                    .orElse(true);
        }

        return true;
    }
    
    private boolean tryHttpShutdown(String endpoint) {
        try {
            HttpURLConnection conn =
                (HttpURLConnection) new URL(endpoint + "/shutdown").openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            return conn.getResponseCode() < 500;
        } catch (Exception e) {
            return false;
        }
    }
}