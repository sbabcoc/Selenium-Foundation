package com.nordstrom.automation.selenium.core;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.apache.http.HttpHost;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.listeners.DriverManager;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.LinkedListeners;
import com.nordstrom.common.base.UncheckedThrow;

@LinkedListeners({DriverManager.class, ExecutionFlowController.class})
public class GridUtilityTest {
    
    private static final int POLL_COUNT = 30;
    private static final long POLL_DELAY = 500;
    private static final String HUB_SHUTDOWN = "/lifecycle-manager?action=shutdown";
    private static final String NODE_SHUTDOWN = "/selenium-server/driver/?cmd=shutDownSeleniumServer";
    
    @BeforeClass
    public void killLocalGrid() throws UnknownHostException, MalformedURLException, InterruptedException {
        SeleniumConfig config = SeleniumConfig.getConfig();
        
        GridHubConfiguration hubConfig = config.getHubConfig();
        RegistrationRequest nodeConfig = config.getNodeConfig();
        
        HttpHost hubHost = GridUtility.getHubHost(hubConfig);
        HttpHost nodeHost = GridUtility.getNodeHost(nodeConfig);
        
        boolean isLocalHub = GridUtility.isThisMyIpAddress(InetAddress.getByName(hubHost.getHostName()));
        boolean isLocalNode = GridUtility.isThisMyIpAddress(InetAddress.getByName(nodeHost.getHostName()));
        
        if (!isLocalHub) throw new IllegalStateException("Configured for non-local hub host");
        if (!isLocalNode) throw new IllegalStateException("Configured for non-local node host");
        
        if (GridUtility.isNodeActive(nodeConfig)) {
            try {
                GridUtility.getHttpResponse(nodeHost, NODE_SHUTDOWN);
                
                int count = POLL_COUNT;
                do {
                    if (!GridUtility.isNodeActive(nodeConfig)) break;
                    if (count-- == 0) throw new IOException("Node still active after 15 seconds");
                    Thread.sleep(POLL_DELAY);
                } while (true);
            } catch (IOException e) {
                throw UncheckedThrow.throwUnchecked(e);
            }
        }
        
        if (GridUtility.isHubActive(hubConfig)) {
            try {
                GridUtility.getHttpResponse(hubHost, HUB_SHUTDOWN);
                
                int count = POLL_COUNT;
                do {
                    if (!GridUtility.isHubActive(hubConfig)) break;
                    if (count-- == 0) throw new IOException("Hub still active after 15 seconds");
                    Thread.sleep(POLL_DELAY);
                } while (true);
            } catch (IOException e) {
                throw UncheckedThrow.throwUnchecked(e);
            }
        }
        
    }
    
    @Test
    @NoDriver
    public void testIsActive() throws UnknownHostException, MalformedURLException {
        SeleniumConfig config = SeleniumConfig.getConfig();
        GridHubConfiguration hubConfig = config.getHubConfig();
        assertFalse(GridUtility.isHubActive(hubConfig), "Configured local hub should initially be inactive");
        assertTrue(GridUtility.isHubActive(), "Configured local hub should have been activated");
    }

}
