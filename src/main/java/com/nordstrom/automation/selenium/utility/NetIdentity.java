package com.nordstrom.automation.selenium.utility;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class NetIdentity {
    
    private static final Pattern INTERNAL = Pattern.compile("(10|172\\.16|192\\.168)\\.");

    private String hostAddress;

    public NetIdentity() {
        try {
            this.hostAddress = findInternalAddress(NetworkInterface.getNetworkInterfaces());
        } catch (SocketException | UnknownHostException e) {
            this.hostAddress = "127.0.0.1";
        }
    }

    private String findInternalAddress(Enumeration<NetworkInterface> interfaces) throws UnknownHostException {
        while (interfaces.hasMoreElements()) {
            NetworkInterface intrface = interfaces.nextElement();
            
            try {
                if ((intrface != null) && intrface.isUp()) {
                    Enumeration<InetAddress> addresses = intrface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        String hostAddr = address.getHostAddress();
   
                        if (INTERNAL.matcher(hostAddr).find(0)) {
                            return hostAddr;
                        }
                    }
                }
            } catch (SocketException eaten) {
                // nothing to do here
            }
        }
        
        return InetAddress.getLocalHost().getHostAddress();
    }

    public String getHostAddress() {
        return hostAddress;
    }
}
