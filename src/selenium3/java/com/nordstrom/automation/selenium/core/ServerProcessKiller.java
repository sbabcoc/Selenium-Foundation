package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.common.file.OSInfo;

/**
 * This class is responsible for shutting down server processes. If the caller doesn't provide a {@link Process}
 * object, this class uses {@link ServerPidFinder} to identify the process that's listening to the specified URL.
 */
public class ServerProcessKiller {
    
    private enum ProcessKiller {
        WINDOWS("taskkill", "/PID", null, "/F"),
        MAC_UNIX("kill", "-9", null);
        
        String[] command;
        
        ProcessKiller(String... command) {
            this.command = command;
        }
        
        String[] getCommand(String pid) {
            String[] command = this.command;
            command[2] = pid;
            return command;
        }
    }
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerProcessKiller.class);
    
    /**
     * Private constructor to prevent instantiation.
     */
    private ServerProcessKiller() {
        throw new AssertionError("ServerProcessKiller is a static utility class that cannot be instantiated");
    }
    
    /**
     * Terminate the server listening to specified 'localhost' URL.
     * 
     * @param process server {@link Process} object (may be {@code null})
     * @param serverUrl {@link URL} of server to terminate
     * @return {@code true} if process was terminated; otherwise {@code false}
     * @throws InterruptedException if this thread was interrupted
     */
    public static boolean killServerProcess(Process process, URL serverUrl) throws InterruptedException {
        Objects.requireNonNull(serverUrl, "[serverUrl] must be non-null");
        if (!GridUtility.isLocalHost(serverUrl)) {
            throw new IllegalArgumentException("Server URL '" + serverUrl.toExternalForm() + "' is not 'localhost'");
        }
        
        if (process != null) {
            process.destroy();
            int exitCode = process.waitFor();
            LOGGER.debug("Terminated local server process listening to: {}", serverUrl);
            return exitCode == 0;
        } else {
            String pid = ServerPidFinder.getPidOfServerAt(serverUrl.getPort());
            if (pid != null) {
                LOGGER.debug("Local server with process ID '{}' listening to: {}", pid, serverUrl);
                try {
                    ProcessKiller command = 
                            OSInfo.getDefault().getType() == OSInfo.OSType.WINDOWS ? ProcessKiller.WINDOWS : ProcessKiller.MAC_UNIX;
                    Process killer = new ProcessBuilder(command.getCommand(pid)).inheritIO().start();
                    int exitCode = killer.waitFor();
                    LOGGER.debug("Terminated local server process listening to: {}", serverUrl);
                    return exitCode == 0;
                } catch (IOException e) {
                    // nothing to do here;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                LOGGER.debug("Failed to identify process listening to: {}", serverUrl);
            }
            return false;
        }
    }
}
