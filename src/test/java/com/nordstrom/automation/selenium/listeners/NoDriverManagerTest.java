package com.nordstrom.automation.selenium.listeners;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static com.nordstrom.automation.selenium.platform.TargetType.WEB_APP_NAME;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.exceptions.DriverNotAvailableException;
import com.nordstrom.automation.selenium.platform.TargetPlatform;
import com.nordstrom.automation.selenium.support.TestNgTargetBase;

public class NoDriverManagerTest extends TestNgTargetBase {

    @BeforeMethod(groups = {"NoDriverBefore"})
    public void beforeMethodNoDriver() {
        assertFalse(nabDriver().isPresent(), "Driver should not have been created");
    }
    
    @NoDriver
    @Test(groups = {"NoBeforeNoDriver"})
    @TargetPlatform(WEB_APP_NAME)
    public void testNoBeforeNoDriver() {
        assertFalse(nabDriver().isPresent(), "Driver should not have been created");
    }
    
    @Test(groups = {"NoDriverBefore"})
    @TargetPlatform(WEB_APP_NAME)
    public void testNoDriverBefore() {
        assertTrue(nabDriver().isPresent(), "Driver should have been created");
    }
    
    @NoDriver
    @Test(groups = {"NoBeforeNoDriver"}, expectedExceptions = {DriverNotAvailableException.class})
    @TargetPlatform(WEB_APP_NAME)
    public void testNoDriverException() {
        getDriver();
    }
    
}
