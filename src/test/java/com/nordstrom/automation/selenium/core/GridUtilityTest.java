package com.nordstrom.automation.selenium.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeoutException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.LinkedListeners;
import com.nordstrom.automation.selenium.listeners.DriverListener;
import com.nordstrom.automation.selenium.support.TestNgBase;

@LinkedListeners({DriverListener.class, ExecutionFlowController.class})
public class GridUtilityTest extends TestNgBase {
    
    @BeforeClass
    public void stopLocalGrid() throws InterruptedException {
        if (!SeleniumConfig.getConfig().shutdownGrid(true)) {
            throw new IllegalStateException("Configured for non-local hub host");
        }
    }
    
    @NoDriver
    @Test(expectedExceptions = {NullPointerException.class},
            expectedExceptionsMessageRegExp = "\\[hostUrl\\] must be non-null")
    public void testHostNullCheck() {
        GridUtility.isHostActive(null, "/");
    }
    
    @NoDriver
    @Test(expectedExceptions = {NullPointerException.class},
            expectedExceptionsMessageRegExp = "\\[request\\] must be non-null")
    public void testRequestNullCheck() throws MalformedURLException {
        URL hostUrl = new URL("http://" + GridUtility.getLocalHost());
        GridUtility.isHostActive(hostUrl, null);
    }
    
    @Test
    @NoDriver
    public void testIsActive() throws IOException, InterruptedException, TimeoutException {
        SeleniumConfig config = SeleniumConfig.getConfig();
        URL hubUrl = config.getHubUrl();
        if (hubUrl != null) {
            assertFalse(GridUtility.isHubActive(hubUrl), "Configured local hub should initially be inactive");
        }
        LocalSeleniumGrid localGrid = (LocalSeleniumGrid) config.getSeleniumGrid();
        hubUrl = config.getHubUrl();
        assertNotNull(hubUrl, "Configuration was not updated with local hub URL");
        assertFalse(GridUtility.isHubActive(hubUrl), "Upon creation, local hub should be inactive");
        localGrid.activate();
        assertTrue(GridUtility.isHubActive(hubUrl), "Local hub should have been activated");
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
