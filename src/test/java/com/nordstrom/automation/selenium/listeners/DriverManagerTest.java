package com.nordstrom.automation.selenium.listeners;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.annotations.PageUrl;
import com.nordstrom.automation.selenium.model.TestNgRoot;

public class DriverManagerTest extends TestNgRoot {
    
    @BeforeMethod(groups = {"WithDriverBefore"})
    @InitialPage(pageUrl=@PageUrl("/grid/admin/ExamplePageServlet"))
    public void beforeMethodWithDriver() {
        assertTrue(nabDriver().isPresent(), "Driver should have been created");
    }
    
    @Test(groups = {"WithDriverBefore"})
    public void testWithDriverBefore() {
        assertTrue(nabDriver().isPresent(), "Driver should have been created");
    }
    
    @NoDriver
    @Test(groups = {"WithDriverBefore"})
    public void testCloseDriverBefore() {
        assertFalse(nabDriver().isPresent(), "Driver should have been closed");
    }
    
}
