package com.nordstrom.automation.selenium.listener;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.Reporter;
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
        assertNotNull(DriverManager.getDriver(Reporter.getCurrentTestResult()), "Driver should have been created");
    }
    
    @Test(groups = {"WithDriverBefore"})
    public void testWithDriverBefore() {
        assertNotNull(DriverManager.getDriver(Reporter.getCurrentTestResult()), "Driver should have been created");
    }
    
    @NoDriver
    @Test(groups = {"WithDriverBefore"})
    public void testCloseDriverBefore() {
        assertNull(DriverManager.getDriver(Reporter.getCurrentTestResult()), "Driver should have been closed");
    }
    
}
