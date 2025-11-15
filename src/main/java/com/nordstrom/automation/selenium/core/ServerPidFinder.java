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
        WINDOWS("cmd.exe", "/c", "for /f \"tokens=5\" %%a in ('netstat -ano ^| findstr :%d ^| findstr LISTENING') do @echo %%a"),
        MAC_UNIX("sh", "-c", "lsof -iTCP:%d -sTCP:LISTEN -t");
        
        private String executable;
        private String commandOption;
        private String commandFormat;

        PidFinder(String execuable, String commandOption, String commandFormat) {
            this.executable = execuable;
            this.commandOption = commandOption;
            this.commandFormat = commandFormat;
        }
        
        String getExecutable() {
            return executable;
        }
        
        String getOption() {
            return commandOption;
        }
        
        String getCommand(int port) {
            return String.format(commandFormat, port);
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
     * @return if found, ID of listening process; otherwise {@code null}
     */
    public static String getPidOfServerAt(int port) {
        String pid = null;
        
        try {
            PidFinder finder = 
                    OSInfo.getDefault().getType() == OSInfo.OSType.WINDOWS ? PidFinder.WINDOWS : PidFinder.MAC_UNIX;
            
            ProcessBuilder pb = new ProcessBuilder(finder.getExecutable(), finder.getOption(), finder.getCommand(port));
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
