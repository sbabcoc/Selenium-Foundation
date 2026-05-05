package com.nordstrom.automation.selenium.utility;

import java.io.IOException;
import java.net.ServerSocket;

import com.nordstrom.automation.selenium.core.ServerPidFinder;

public class GridHubPortAllocator {

    public static class GridPorts {
        public final int hubPort;
        public final int eventBusPubPort;
        public final int eventBusSubPort;

        public GridPorts(int hubPort, int pub, int sub) {
            this.hubPort = hubPort;
            this.eventBusPubPort = pub;
            this.eventBusSubPort = sub;
        }

        @Override
        public String toString() {
            return "hub=" + hubPort +
                    ", pub=" + eventBusPubPort +
                    ", sub=" + eventBusSubPort;
        }
    }

    /**
     * Allocates a full Selenium Grid hub port set atomically.
     */
    public static GridPorts allocate(int startHubPort) {
        int hub = startHubPort;

        while (hub < 65530) {

            int pub = hub - 2;
            int sub = hub - 1;

            if (pub < 1024) {
                hub += 10;
                continue;
            }

            if (isFree(hub, pub, sub)) {
                Reservation r = reserve(hub, pub, sub);
                if (r.success) {
                    r.close(); // release locks, ports remain reserved by OS rules
                    return new GridPorts(hub, pub, sub);
                }
            }

            hub += 10; // spacing reduces collision probability
        }

        throw new RuntimeException("No free Grid port set found");
    }

    // -------------------------
    // atomic reservation
    // -------------------------

    private static Reservation reserve(int hub, int pub, int sub) {
        try {
            ServerSocket h = new ServerSocket(hub);
            ServerSocket p = new ServerSocket(pub);
            ServerSocket s = new ServerSocket(sub);
            return new Reservation(true, h, p, s);
        } catch (IOException e) {
            return new Reservation(false, null, null, null);
        }
    }
    
    public static GridPorts assigned(int hubPort) {
        int pubPort = hubPort - 2;
        int subPort = hubPort - 1;
        
        String hubPid = ServerPidFinder.getPidOfServerAt(hubPort, true);
        String pubPid = ServerPidFinder.getPidOfServerAt(pubPort, false);
        String subPid = ServerPidFinder.getPidOfServerAt(subPort, true);

        
    }
    
    public static boolean isFree(int hub, int pub, int sub) {
        return (isFree(hub) && isFree(pub) && isFree(sub));
    }

    public static boolean isFree(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static class Reservation {
        final boolean success;
        final ServerSocket hub;
        final ServerSocket pub;
        final ServerSocket sub;

        Reservation(boolean success, ServerSocket hub, ServerSocket pub, ServerSocket sub) {
            this.success = success;
            this.hub = hub;
            this.pub = pub;
            this.sub = sub;
        }

        void close() {
            closeQuiet(hub);
            closeQuiet(pub);
            closeQuiet(sub);
        }

        private void closeQuiet(ServerSocket s) {
            if (s != null) {
                try { s.close(); } catch (IOException ignored) {}
            }
        }
    }
}
