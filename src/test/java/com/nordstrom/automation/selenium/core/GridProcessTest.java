package com.nordstrom.automation.selenium.core;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.NoDriver;

public class GridProcessTest {

    @NoDriver
    @Test(expectedExceptions = {AssertionError.class},
            expectedExceptionsMessageRegExp = "GridProcess is a static utility class that cannot be instantiated")
    public void testPrivateConstructor() throws Throwable {
        
        Constructor<?>[] ctors;
        ctors = LocalGrid.class.getDeclaredConstructors();
        assertEquals(ctors.length, 1, "GridProcess must have exactly one constructor");
        assertEquals(ctors[0].getModifiers() & Modifier.PRIVATE, Modifier.PRIVATE,
                        "GridProcess constructor must be private");
        assertEquals(ctors[0].getParameterTypes().length, 0, "GridProcess constructor must have no arguments");
        
        try {
            ctors[0].setAccessible(true);
            ctors[0].newInstance();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
    
}
