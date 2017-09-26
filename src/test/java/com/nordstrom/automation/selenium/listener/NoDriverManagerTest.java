package com.nordstrom.automation.selenium.listener;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.exceptions.DriverNotAvailableException;
import com.nordstrom.automation.selenium.listeners.DriverManager;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.LinkedListeners;

@LinkedListeners({DriverManager.class, ExecutionFlowController.class})
public class NoDriverManagerTest {

    @BeforeMethod(groups = {"NoDriverBefore"})
    public void beforeMethodNoDriver() {
        assertFalse(DriverManager.findDriver().isPresent(), "Driver should not have been created");
    }
    
    @NoDriver
    @Test(groups = {"NoBeforeNoDriver"})
    public void testNoBeforeNoDriver() {
        assertFalse(DriverManager.findDriver().isPresent(), "Driver should not have been created");
    }
    
    @Test(groups = {"NoDriverBefore"})
    public void testNoDriverBefore() {
        assertTrue(DriverManager.findDriver().isPresent(), "Driver should have been created");
    }
    
    @NoDriver
    @Test(groups = {"NoBeforeNoDriver"}, expectedExceptions = {DriverNotAvailableException.class})
    public void testNoDriverException() {
        DriverManager.getDriver();
    }
    
}
