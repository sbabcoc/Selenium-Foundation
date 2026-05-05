package com.nordstrom.automation.selenium.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import com.nordstrom.common.file.OSInfo;

/**
 * This class uses OS utilities to identify the ID of the server process listening to the specified port.
 */
public class ServerPidFinder {

    private enum PidFinder {
        WINDOWS("cmd.exe",
                "/c",
                "for /f \"tokens=5\" %%a in ('netstat -ano ^| findstr :%d ^| findstr LISTENING') do @echo %%a",
                "for /f \"tokens=5\" %%a in ('netstat -ano ^| findstr :%d') do @echo %%a"),
        MAC_UNIX("sh",
                "-c",
                "lsof -nP -iTCP:%d -sTCP:LISTEN -t",
                "lsof -nP -iTCP:%d -t");
        
        private String executable;
        private String commandOption;
        private String listenMode;
        private String anyMode;

        PidFinder(String execuable, String commandOption, String listenMode, String anyMode) {
            this.executable = execuable;
            this.commandOption = commandOption;
            this.listenMode = listenMode;
            this.anyMode = anyMode;
        }
        
        String getExecutable() {
            return executable;
        }
        
        String getOption() {
            return commandOption;
        }
        
        String getCommand(int port, boolean listen) {
            return String.format(listen ? listenMode : anyMode, port);
        }
    }
    
    /**
     * Private constructor to prevent instantiation.
     */
    private ServerPidFinder() {
        throw new AssertionError("ServerPidFinder is a static utility class that cannot be instantiated");
    }
    
    /**
     * Get the process ID of the server listening to the specified port.
     * 
     * @param port {@code localhost} port to check
     * @param listen {@code true} to require LISTEN mode; {@code false} to accept any mode
     * @return if found, ID of listening process; otherwise {@code null}
     */
    public static String getPidOfServerAt(int port, boolean listen) {
        String pid = null;
        
        try {
            PidFinder finder = 
                    OSInfo.getDefault().getType() == OSInfo.OSType.WINDOWS ? PidFinder.WINDOWS : PidFinder.MAC_UNIX;
            ProcessBuilder pb = 
                    new ProcessBuilder(finder.getExecutable(), finder.getOption(), finder.getCommand(port, listen));
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                pid = Optional.ofNullable(reader.readLine()).filter(s -> s != null && !s.matches("\\s*")).map(String::trim).orElse(null);
            }

            process.waitFor();
        } catch (IOException e) {
            // nothing to do here;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return pid;
    }
}
