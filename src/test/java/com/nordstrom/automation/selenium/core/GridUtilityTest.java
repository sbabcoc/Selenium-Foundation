package com.nordstrom.automation.selenium.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.openqa.selenium.net.UrlChecker;
import org.openqa.selenium.net.UrlChecker.TimeoutException;
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
    
    private static final long SHUTDOWN_DELAY = 15;
    private static final String HUB_SHUTDOWN = "/lifecycle-manager?action=shutdown";
    private static final String NODE_SHUTDOWN = "/selenium-server/driver/?cmd=shutDownSeleniumServer";
    
    @BeforeClass
    public void killLocalGrid() throws UnknownHostException, TimeoutException {
        SeleniumConfig config = SeleniumConfig.getConfig();
        
        GridHubConfiguration hubConfig = config.getHubConfig();
        RegistrationRequest nodeConfig = config.getNodeConfig();
        
        HttpHost hubHost = GridUtility.getHubHost(hubConfig);
        HttpHost nodeHost = GridUtility.getNodeHost(nodeConfig);
        
        boolean isLocalHub = GridUtility.isThisMyIpAddress(InetAddress.getByName(hubHost.getHostName()));
        boolean isLocalNode = GridUtility.isThisMyIpAddress(InetAddress.getByName(nodeHost.getHostName()));
        
        if (!isLocalHub) {
            throw new IllegalStateException("Configured for non-local hub host");
        }
        if (!isLocalNode) {
            throw new IllegalStateException("Configured for non-local node host");
        }
        
        UrlChecker urlChecker = new UrlChecker();
        
        if (GridUtility.isNodeActive(nodeConfig)) {
            try {
                GridUtility.getHttpResponse(nodeHost, NODE_SHUTDOWN);
                urlChecker.waitUntilUnavailable(SHUTDOWN_DELAY, TimeUnit.SECONDS, URI.create(nodeHost.toURI()).toURL());
            } catch (IOException e) {
                throw UncheckedThrow.throwUnchecked(e);
            }
        }
        
        if (GridUtility.isHubActive(hubConfig)) {
            try {
                GridUtility.getHttpResponse(hubHost, HUB_SHUTDOWN);
                urlChecker.waitUntilUnavailable(SHUTDOWN_DELAY, TimeUnit.SECONDS, URI.create(hubHost.toURI()).toURL());
            } catch (IOException e) {
                throw UncheckedThrow.throwUnchecked(e);
            }
        }
        
    }
    
    @Test
    @NoDriver
    public void testIsActive() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        GridHubConfiguration hubConfig = config.getHubConfig();
        assertFalse(GridUtility.isHubActive(hubConfig), "Configured local hub should initially be inactive");
        assertTrue(GridUtility.isHubActive(), "Configured local hub should have been activated");
    }
    
    @NoDriver
    @Test(expectedExceptions = {AssertionError.class},
            expectedExceptionsMessageRegExp = "GridUtility is a static utility class that cannot be instantiated")
    public void testPrivateConstructor() throws Throwable {
        
        Constructor<?>[] ctors;
        ctors = GridUtility.class.getDeclaredConstructors();
        assertEquals(ctors.length, 1, "GridUtility must have exactly one constructor");
        assertEquals(ctors[0].getModifiers() & Modifier.PRIVATE, Modifier.PRIVATE,
                        "GridUtility constructor must be private");
        assertEquals(ctors[0].getParameterTypes().length, 0, "GridUtility constructor must have no arguments");
        
        try {
            ctors[0].setAccessible(true);
            ctors[0].newInstance();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
    
}
