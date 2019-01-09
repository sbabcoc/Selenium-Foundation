package com.nordstrom.automation.selenium.core;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
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
    
    @Test
    @NoDriver
    public void testIsActive() throws IOException, InterruptedException, TimeoutException {
        SeleniumConfig config = SeleniumConfig.getConfig();
        assertFalse(GridUtility.isHubActive(config.getHubUrl()), "Configured local hub should initially be inactive");
        config.getSeleniumGrid();
        assertTrue(GridUtility.isHubActive(config.getHubUrl()), "Configured local hub should have been activated");
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
