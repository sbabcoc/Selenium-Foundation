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
import com.nordstrom.automation.selenium.exceptions.PlatformActivationFailedException;
import com.nordstrom.automation.selenium.model.ExamplePage;
import com.nordstrom.automation.selenium.platform.ExamplePlatform;
import com.nordstrom.automation.selenium.platform.TargetPlatform;

@InitialPage(ExamplePage.class)
public class JUnitPlatformTest extends JUnitPlatformBase<ExamplePlatform> {

    public JUnitPlatformTest() {
        super(ExamplePlatform.class);
    }
    
    @Test
    public void testDefaultPlatform() {
        assertTrue(getTargetPlatform().matches(ExamplePlatform.PHASE1_NAME));
        assertEquals("green", getTargetPlatform().getColor());
    }
    
    @Test
    @TargetPlatform(ExamplePlatform.PHASE2_NAME)
    public void testPlatformTwo() {
        assertTrue(getTargetPlatform().matches(ExamplePlatform.PHASE2_NAME));
        assertEquals("amber", getTargetPlatform().getColor());
    }
    
    @Override
    public void activatePlatform(WebDriver driver, ExamplePlatform platform)
            throws PlatformActivationFailedException {
        
        Object runner = LifecycleHooks.getRunnerForTarget(this);
        AtomicTest<FrameworkMethod> test = LifecycleHooks.getAtomicTestOf(runner);
        FrameworkMethod method = test.getIdentity();
        
        ExamplePlatform expected = null;
        switch(method.getName()) {
        case "testDefaultPlatform":
            expected = ExamplePlatform.PHASE1;
            break;
        case "testPlatformTwo":
            expected = ExamplePlatform.PHASE2;
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
    public ExamplePlatform getDefaultPlatform() {
        return ExamplePlatform.PHASE1;
    }
}
