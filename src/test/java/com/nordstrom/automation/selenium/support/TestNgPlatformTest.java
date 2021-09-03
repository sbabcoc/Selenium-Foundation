package com.nordstrom.automation.selenium.support;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.exceptions.PlatformActivationFailedException;
import com.nordstrom.automation.selenium.model.ExamplePage;
import com.nordstrom.automation.selenium.platform.Transition;
import com.nordstrom.automation.selenium.platform.TargetPlatform;

@InitialPage(ExamplePage.class)
public class TestNgPlatformTest extends TestNgPlatformBase<Transition> {

    public TestNgPlatformTest() {
        super(Transition.class);
    }
    
    @Test
    public void testDefaultPlatform() {
        assertTrue(getTargetPlatform().matches(Transition.PHASE1_NAME));
        assertEquals(getTargetPlatform().getColor(), "green");
    }
    
    @Test
    @TargetPlatform(Transition.PHASE2_NAME)
    public void testPlatformTwo() {
        assertTrue(getTargetPlatform().matches(Transition.PHASE2_NAME));
        assertEquals(getTargetPlatform().getColor(), "amber");
    }
    
    @Override
    public void activatePlatform(WebDriver driver, Transition platform)
            throws PlatformActivationFailedException {
        
        ITestResult result = Reporter.getCurrentTestResult();
        ITestNGMethod method = result.getMethod();
        
        Transition expected = null;
        switch(method.getMethodName()) {
        case "testDefaultPlatform":
            expected = Transition.PHASE1;
            break;
        case "testPlatformTwo":
            expected = Transition.PHASE2;
            break;
        default:
            throw new RuntimeException("Unexpected method: " + method.getMethodName());
        }
        
        if (platform != expected) {
            throw new PlatformActivationFailedException(platform, "expected: " + expected.getName());
        }
        
        // perform some platform-related activation
        driver.manage().addCookie(new Cookie("color", platform.getColor()));
    }

    @Override
    public Transition getDefaultPlatform() {
        return Transition.PHASE1;
    }
}
