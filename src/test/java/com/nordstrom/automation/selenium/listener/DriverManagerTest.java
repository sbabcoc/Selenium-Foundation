package com.nordstrom.automation.selenium.listener;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.annotations.PageUrl;
import com.nordstrom.automation.selenium.listeners.DriverManager;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.LinkedListeners;

@LinkedListeners({DriverManager.class, ExecutionFlowController.class})
public class DriverManagerTest {
    
    @InitialPage(pageUrl=@PageUrl(scheme="file", value="ExamplePage.html"))
    @BeforeMethod(groups = {"WithDriverBefore"})
    public void beforeMethodWithDriver() {
        assertTrue(DriverManager.findDriver().isPresent(), "Driver should have been created");
    }
    
    @Test(groups = {"WithDriverBefore"})
    public void testWithDriverBefore() {
        assertTrue(DriverManager.findDriver().isPresent(), "Driver should have been created");
    }
    
    @NoDriver
    @Test(groups = {"WithDriverBefore"})
    public void testCloseDriverBefore() {
        assertFalse(DriverManager.findDriver().isPresent(), "Driver should have been closed");
    }
    
}
