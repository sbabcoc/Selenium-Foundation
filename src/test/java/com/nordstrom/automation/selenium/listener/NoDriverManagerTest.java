package com.nordstrom.automation.selenium.listener;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.exceptions.DriverNotAvailableException;
import com.nordstrom.automation.selenium.support.TestNGBase;

public class NoDriverManagerTest extends TestNGBase {

    @BeforeMethod(groups = {"NoDriverBefore"})
    public void beforeMethodNoDriver() {
        assertFalse(nabDriver().isPresent(), "Driver should not have been created");
    }
    
    @NoDriver
    @Test(groups = {"NoBeforeNoDriver"})
    public void testNoBeforeNoDriver() {
        assertFalse(nabDriver().isPresent(), "Driver should not have been created");
    }
    
    @Test(groups = {"NoDriverBefore"})
    public void testNoDriverBefore() {
        assertTrue(nabDriver().isPresent(), "Driver should have been created");
    }
    
    @NoDriver
    @Test(groups = {"NoBeforeNoDriver"}, expectedExceptions = {DriverNotAvailableException.class})
    public void testNoDriverException() {
        getDriver();
    }
    
}
