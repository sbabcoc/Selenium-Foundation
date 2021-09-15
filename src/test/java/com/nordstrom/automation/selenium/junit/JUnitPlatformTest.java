package com.nordstrom.automation.selenium.junit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.junit.AtomicTest;
import com.nordstrom.automation.junit.LifecycleHooks;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.example.ExamplePage;
import com.nordstrom.automation.selenium.exceptions.PlatformActivationFailedException;
import com.nordstrom.automation.selenium.platform.Transition;
import com.nordstrom.automation.selenium.platform.TargetPlatform;

@InitialPage(ExamplePage.class)
public class JUnitPlatformTest extends JUnitPlatformBase<Transition> {

    public JUnitPlatformTest() {
        super(Transition.class);
    }
    
    @Test
    public void testDefaultPlatform() {
        assertTrue(getTargetPlatform().matches(Transition.PHASE1_NAME));
        assertEquals("green", getTargetPlatform().getColor());
    }
    
    @Test
    @TargetPlatform(Transition.PHASE2_NAME)
    public void testPlatformTwo() {
        assertTrue(getTargetPlatform().matches(Transition.PHASE2_NAME));
        assertEquals("amber", getTargetPlatform().getColor());
    }
    
    @Override
    public void activatePlatform(WebDriver driver, Transition platform)
            throws PlatformActivationFailedException {
        
        AtomicTest test = LifecycleHooks.getAtomicTestOf(this);
        FrameworkMethod method = test.getIdentity();
        
        Transition expected = null;
        switch(method.getName()) {
        case "testDefaultPlatform":
            expected = Transition.PHASE1;
            break;
        case "testPlatformTwo":
            expected = Transition.PHASE2;
            break;
        default:
            throw new RuntimeException("Unexpected method: " + method.getName());
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
