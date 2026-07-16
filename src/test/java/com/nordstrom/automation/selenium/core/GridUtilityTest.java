package com.nordstrom.automation.selenium.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.LinkedListeners;
import com.nordstrom.automation.selenium.listeners.DriverListener;
import com.nordstrom.automation.selenium.support.TestNgBase;

@Test
@LinkedListeners({DriverListener.class, ExecutionFlowController.class})
public class GridUtilityTest extends TestNgBase {
    
    @NoDriver
    @Test(expectedExceptions = {NullPointerException.class},
            expectedExceptionsMessageRegExp = "\\[hostUrl\\] must be non-null")
    public void testHostNullCheck() {
        GridUtility.isHostActive(null, "/");
    }
    
    @NoDriver
    @Test
    public void testHostWithoutRequest() throws MalformedURLException {
        URI hostUri = URI.create("https://github.com");
        assertTrue(GridUtility.isHostActive(hostUri.toURL()), "Failed activity check for: " + hostUri);
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
    
    @NoDriver
    @Test
    public void testIsSelenium4Hub_negative() throws MalformedURLException {
        URI hostUri = URI.create("https://github.com");
        assertFalse(GridUtility.isSelenium4Hub(hostUri.toURL()),
                "github.com should not be identified as a Selenium 4 hub");
    }
    
    @NoDriver
    @Test
    public void testIsSelenium3Hub_negative() throws MalformedURLException {
        URI hostUri = URI.create("https://github.com");
        assertFalse(GridUtility.isSelenium3Hub(hostUri.toURL()),
                "github.com should not be identified as a Selenium 3 hub");
    }
    
    @NoDriver
    @Test
    public void testIsSelenium4Hub_nullCheck() {
        assertFalse(GridUtility.isSelenium4Hub(null),
                "isSelenium4Hub() should return false for null URL");
    }

    @NoDriver
    @Test
    public void testIsSelenium3Hub_nullCheck() {
        assertFalse(GridUtility.isSelenium3Hub(null),
                "isSelenium3Hub() should return false for null URL");
    }
}
