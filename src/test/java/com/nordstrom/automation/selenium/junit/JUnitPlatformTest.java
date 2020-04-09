package com.nordstrom.automation.selenium.junit;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.nordstrom.automation.selenium.platform.ExamplePlatform;
import com.nordstrom.automation.selenium.platform.TargetPlatform;

public class JUnitPlatformTest extends JUnitPlatformBase<ExamplePlatform> {

    public JUnitPlatformTest() {
        super(ExamplePlatform.class);
    }
    
    @Test
    public void testDefaultPlatform() {
        assertTrue(getTargetPlatform().matches(ExamplePlatform.PLATFORM_ONE_NAME));
    }
    
    @Test
    @TargetPlatform(ExamplePlatform.PLATFORM_TWO_NAME)
    public void testPlatformTwo() {
        assertTrue(getTargetPlatform().matches(ExamplePlatform.PLATFORM_TWO_NAME));
    }

    @Override
    public ExamplePlatform getDefaultPlatform() {
        return ExamplePlatform.PLATFORM_ONE;
    }

}
